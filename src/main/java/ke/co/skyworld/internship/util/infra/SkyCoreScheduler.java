package ke.co.skyworld.internship.util.infra;


import ke.co.skyworld.internship.util.logging.Log;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.time.Instant;
import java.util.*;


public class SkyCoreScheduler {
    private static SkyCoreScheduler
            instance;
    private final Scheduler SkyCoreScheduler;
    private final Map<String, JobConsumer> jobHandlers;
    private boolean autoStart = true;

    public SkyCoreScheduler
            () throws SchedulerException {
        this.SkyCoreScheduler
                = StdSchedulerFactory.getDefaultScheduler();
        this.jobHandlers = new HashMap<>();
        GenericJob.setInstance(this);
        instance = this;
    }

    public SkyCoreScheduler
            (QuartzProperties properties) throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize(properties.toProperties());
        this.SkyCoreScheduler
                = factory.getScheduler();
        this.jobHandlers = new HashMap<>();
        GenericJob.setInstance(this);
        instance = this;
    }

    public static SkyCoreScheduler
    getInstance() {
        return instance;
    }

    public void start() throws SchedulerException {
        if (!SkyCoreScheduler
                .isStarted()) {
            SkyCoreScheduler
                    .start();
            Log.info(this.getClass(), "start", "SkyCoreScheduler" +
                    " started");
        }
    }

    public void shutdown() throws SchedulerException {
        if (!SkyCoreScheduler
                .isShutdown()) {
            SkyCoreScheduler
                    .shutdown();
            Log.info(this.getClass(), "shutdown", "SkyCoreScheduler" +
                    " shutdown");
        }
    }

    public boolean isRunning() throws SchedulerException {
        return SkyCoreScheduler
                .isStarted() && !SkyCoreScheduler
                .isShutdown();
    }

    public void registerHandler(String jobType, JobConsumer handler) {
        jobHandlers.put(jobType, handler);
        Log.info(this.getClass(), "registerHandler", "Registered handler for: " + jobType);
    }

    public String scheduleOneTime(String jobId, String group, Instant fireTime,
                                  JobConsumer handler) throws SchedulerException {
        return scheduleOneTime(jobId, group, fireTime, handler, null);
    }

    public String scheduleOneTime(String jobId, String group, Instant fireTime,
                                  JobConsumer handler, Map<String, Object> jobData)
            throws SchedulerException {

        String effectiveJobId = jobId != null ? jobId : UUID.randomUUID().toString();
        String effectiveGroup = group != null ? group : "DEFAULT";

        JobKey jobKey = JobKey.jobKey(effectiveJobId, effectiveGroup);

        // Cancel existing if present
        if (SkyCoreScheduler
                .checkExists(jobKey)) {
            SkyCoreScheduler
                    .deleteJob(jobKey);
            Log.info(this.getClass(), "scheduleOneTime", "Replaced existing job: " + effectiveJobId);
        }

        // Register handler if provided
        if (handler != null) {
            registerHandler(effectiveJobId, handler);
        }

        // Build job
        JobDetail job = JobBuilder.newJob(GenericJob.class)
                .withIdentity(jobKey)
                .usingJobData("jobHandlerId", effectiveJobId)
                .build();

        // Add custom data
        if (jobData != null) {
            job.getJobDataMap().put("jobData", jobData);
        }

        // Build trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + effectiveJobId, effectiveGroup)
                .startAt(Date.from(fireTime))
                .build();

        SkyCoreScheduler
                .scheduleJob(job, trigger);

        Log.info(this.getClass(), "scheduleOneTime",
                "Scheduled job '" + effectiveJobId + "' at " + fireTime);

        return effectiveJobId;
    }

    public String scheduleCron(String jobId, String group, String cronExpression,
                               JobConsumer handler) throws SchedulerException {
        return scheduleCron(jobId, group, cronExpression, handler, null);
    }

    public String scheduleCron(String jobId, String group, String cronExpression,
                               JobConsumer handler, Map<String, Object> jobData)
            throws SchedulerException {

        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }

        String effectiveJobId = jobId != null ? jobId : UUID.randomUUID().toString();
        String effectiveGroup = group != null ? group : "DEFAULT";

        JobKey jobKey = JobKey.jobKey(effectiveJobId, effectiveGroup);

        // Cancel existing if present
        if (SkyCoreScheduler
                .checkExists(jobKey)) {
            SkyCoreScheduler
                    .deleteJob(jobKey);
        }

        // Register handler
        if (handler != null) {
            registerHandler(effectiveJobId, handler);
        }

        // Build job
        JobDetail job = JobBuilder.newJob(GenericJob.class)
                .withIdentity(jobKey)
                .usingJobData("jobHandlerId", effectiveJobId)
                .build();

        if (jobData != null) {
            job.getJobDataMap().put("jobData", jobData);
        }

        // Build cron trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("cron_" + effectiveJobId, effectiveGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        SkyCoreScheduler
                .scheduleJob(job, trigger);

        Log.info(this.getClass(), "scheduleCron",
                "Scheduled cron job '" + effectiveJobId + "' with expression: " + cronExpression);

        return effectiveJobId;
    }

    public String scheduleDelayed(String jobId, String group, long delaySeconds,
                                  JobConsumer handler) throws SchedulerException {
        return scheduleOneTime(jobId, group, Instant.now().plusSeconds(delaySeconds), handler);
    }

    public <T extends Job> String scheduleConcreteJob(
            Class<T> jobClass,
            String jobId,
            String group,
            Instant fireTime,
            Map<String, Object> jobData) throws SchedulerException {

        String effectiveJobId = jobId != null ? jobId : UUID.randomUUID().toString();
        String effectiveGroup = group != null ? group : "DEFAULT";
        JobKey jobKey = JobKey.jobKey(effectiveJobId, effectiveGroup);

        if (SkyCoreScheduler
                .checkExists(jobKey)) {
            SkyCoreScheduler
                    .deleteJob(jobKey);
        }

        JobBuilder builder = JobBuilder.newJob(jobClass).withIdentity(jobKey);
        if (jobData != null) {
            jobData.forEach((k, v) -> {
                switch (v) {
                    case Long l -> builder.usingJobData(k, l);
                    case Integer i -> builder.usingJobData(k, i);
                    case String s -> builder.usingJobData(k, s);
                    case Boolean b -> builder.usingJobData(k, b);
                    case null, default -> builder.usingJobData(k, String.valueOf(v));
                }
            });
        }
        JobDetail job = builder.build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger_" + effectiveJobId, effectiveGroup)
                .startAt(Date.from(fireTime))
                .build();

        SkyCoreScheduler
                .scheduleJob(job, trigger);

        Log.info(this.getClass(), "scheduleConcreteJob",
                "Scheduled " + jobClass.getSimpleName()
                        + " '" + effectiveJobId + "' at " + fireTime);

        return effectiveJobId;
    }

    public boolean cancelJob(String jobId, String group) throws SchedulerException {
        String effectiveGroup = group != null ? group : "DEFAULT";
        JobKey jobKey = JobKey.jobKey(jobId, effectiveGroup);

        if (SkyCoreScheduler
                .checkExists(jobKey)) {
            boolean deleted = SkyCoreScheduler
                    .deleteJob(jobKey);
            if (deleted) {
                Log.info(this.getClass(), "cancelJob", "Cancelled job: " + jobId);
                jobHandlers.remove(jobId);
            }
            return deleted;
        }
        return false;
    }

    public void pauseJob(String jobId, String group) throws SchedulerException {
        String effectiveGroup = group != null ? group : "DEFAULT";
        SkyCoreScheduler
                .pauseJob(JobKey.jobKey(jobId, effectiveGroup));
        Log.info(this.getClass(), "pauseJob", "Paused job: " + jobId);
    }

    public void resumeJob(String jobId, String group) throws SchedulerException {
        String effectiveGroup = group != null ? group : "DEFAULT";
        SkyCoreScheduler
                .resumeJob(JobKey.jobKey(jobId, effectiveGroup));
        Log.info(this.getClass(), "resumeJob", "Resumed job: " + jobId);
    }

    public boolean jobExists(String jobId, String group) throws SchedulerException {
        String effectiveGroup = group != null ? group : "DEFAULT";
        return SkyCoreScheduler
                .checkExists(JobKey.jobKey(jobId, effectiveGroup));
    }

    public List<JobInfo> getAllJobs() throws SchedulerException {
        List<JobInfo> jobs = new ArrayList<>();

        for (String groupName : SkyCoreScheduler
                .getJobGroupNames()) {
            for (JobKey jobKey : SkyCoreScheduler
                    .getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                JobDetail jobDetail = SkyCoreScheduler
                        .getJobDetail(jobKey);
                List<? extends Trigger> triggers = SkyCoreScheduler
                        .getTriggersOfJob(jobKey);

                JobInfo info = new JobInfo();
                info.jobId = jobKey.getName();
                info.group = jobKey.getGroup();
                info.nextFireTime = triggers.isEmpty() ? null :
                        triggers.getFirst().getNextFireTime();
                info.previousFireTime = triggers.isEmpty() ? null :
                        triggers.getFirst().getPreviousFireTime();

                jobs.add(info);
            }
        }

        return jobs;
    }

    public void clearAllJobs() throws SchedulerException {
        SkyCoreScheduler
                .clear();
        jobHandlers.clear();
        Log.info(this.getClass(), "clearAllJobs", "Cleared all jobs");
    }

    @FunctionalInterface
    public interface JobConsumer {
        void execute(Map<String, Object> jobData) throws Exception;
    }

    public static class GenericJob implements Job {
        private static SkyCoreScheduler
                instance;

        public static void setInstance(SkyCoreScheduler
                                               scheduler) {
            instance = scheduler;
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                JobDataMap dataMap = context.getMergedJobDataMap();
                String handlerId = dataMap.getString("jobHandlerId");

                @SuppressWarnings("unchecked")
                Map<String, Object> jobData = (Map<String, Object>) dataMap.get("jobData");

                Log.info(this.getClass(), "execute", "Executing job: " + handlerId);

                if (instance != null) {
                    JobConsumer handler = instance.jobHandlers.get(handlerId);
                    if (handler != null) {
                        handler.execute(jobData != null ? jobData : Map.of());
                    } else {
                        Log.warning(this.getClass(), "execute",
                                "No handler registered for: " + handlerId);
                    }
                }
            } catch (Exception e) {
                Log.error(this.getClass(), "execute",
                        "Job execution failed: " + e.getMessage(), e);
                throw new JobExecutionException(e);
            }
        }
    }

    public static class JobInfo {
        public String jobId;
        public String group;
        public Date nextFireTime;
        public Date previousFireTime;

        @Override
        public String toString() {
            return String.format("Job{id='%s', group='%s', nextFire=%s, prevFire=%s}",
                    jobId, group, nextFireTime, previousFireTime);
        }
    }

    public static class QuartzProperties {
        private final Map<String, String> properties = new HashMap<>();

        public QuartzProperties() {
            properties.put("org.quartz.scheduler.instanceName", "SkyCoreScheduler");
            properties.put("org.quartz.threadPool.threadCount", "5");
            properties.put("org.quartz.threadPool.threadPriority", "5");
            properties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        }

        public QuartzProperties withThreadCount(int count) {
            properties.put("org.quartz.threadPool.threadCount", String.valueOf(count));
            return this;
        }

        public QuartzProperties withInstanceName(String name) {
            properties.put("org.quartz.scheduler.instanceName", name);
            return this;
        }

        public QuartzProperties usePersistentStore(String dataSource) {
            properties.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
            properties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
            properties.put("org.quartz.jobStore.tablePrefix", "scheduling.qrtz_");
            properties.put("org.quartz.jobStore.isClustered", "true");
            properties.put("org.quartz.scheduler.instanceId", "AUTO");
            properties.put("org.quartz.jobStore.dataSource", dataSource);
            properties.put("org.quartz.dataSource." + dataSource + ".connectionProvider.class",
                    "ke.co.skyworld.internship.skycore.util.db.SkyCoreConnectionProvider");
            return this;
        }

        public Properties toProperties() {
            Properties props = new Properties();
            props.putAll(properties);
            return props;
        }
    }
}
