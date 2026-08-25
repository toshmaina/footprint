package ke.co.skyworld.internship.domain.enums;

public enum LoggingLevel {

    DEBUG("DEBUG"),
    INFO("INFO"),
    WARNING("WARNING"),
    ERROR("ERROR");

    private String value;

    LoggingLevel(String value) {
        this.value = value;
    }

    public static LoggingLevel parse(String value) {
        for (LoggingLevel loggingLevel : LoggingLevel.values()) {
            if (loggingLevel.value().equalsIgnoreCase(value)) {
                return loggingLevel;
            }
        }
        //default
        return LoggingLevel.INFO;
    }

    public String value() {
        return value;
    }

}
