package ke.co.skyworld.internship.config;



import ke.co.skyworld.internship.util.formatting.XmlUtils;
import ke.co.skyworld.internship.util.security.CredentialVault;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;



public class Constants {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
    public static final String TIMESTAMP_MS_FORMAT = "yyyyMMddHHmmssSSS";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String JUST_NOW = "Just now";

    public static final String applicationJson = "application/json";
    public static final String applicationXml = "application/xml";
    public static final String textXml = "text/xml";
    public static final String LOCAL_CONF_FILENAME = "conf.xml";
    public static final String SSA_DELIMITER = "&s*k@y{$d}$&";

    public static final String XML_PATH_TO_LOG_LEVEL = "/api/@log_level";

    public static final String XML_PATH_TO_KEY_SALT = "/api/security/@key_salt";

    public static final String XML_PATH_TO_API_REQUEST_DUMP = "/api/@request_dump";
    public static final String XML_PATH_TO_CONTEXT_PATH = "/api/context/path";
    public static final String XML_PATH_TO_CONTEXT_HOST = "/api/context/host";
    public static final String XML_PATH_TO_CONTEXT_PORT = "/api/context/port";
    public static final String XML_PATH_TO_IO_THREAD_POOL = "/api/context/undertow/@io_thread_pool";
    public static final String XML_PATH_TO_WORKER_THREAD_POOL = "/api/context/undertow/@worker_thread_pool";

    public static final String XML_PATH_TO_DB_HOST = "/api/database/host";
    public static final String XML_PATH_TO_DB_PORT = "/api/database/port";
    public static final String XML_PATH_TO_DB_NAME = "/api/database/name";
    public static final String XML_PATH_TO_DB_USER = "/api/database/user";
    public static final String XML_PATH_TO_DB_PASS = "/api/database/password";
    public static final String XML_PATH_TO_DB_POOL_MIN = "/api/database/pool_min";
    public static final String XML_PATH_TO_DB_POOL_MAX = "/api/database/pool_max";
    public static final String XML_PATH_TO_DB_TIMEOUT = "/api/database/checkout_timeout_seconds";

    public static final String XML_PATH_TO_AUTH_MULTIPLE_SESSIONS = "/api/authentication/@multiple_same_user_sessions";
    public static final String XML_PATH_TO_AUTH_TOKEN_LENGTH = "/api/authentication/access_token/length";
    public static final String XML_PATH_TO_AUTH_TOKEN_TIMEOUT = "/api/authentication/access_token/timeout";
    public static final String XML_PATH_TO_AUTH_TOKEN_TIMEOUT_UNIT = "/api/authentication/access_token/timeout/@time_unit";
    public static final String XML_PATH_TO_REFRESH_TOKEN_LENGTH = "/api/authentication/refresh_token/length";
    public static final String XML_PATH_TO_REFRESH_TOKEN_TIMEOUT = "/api/authentication/refresh_token/timeout";
    public static final String XML_PATH_TO_REFRESH_TOKEN_TIMEOUT_UNIT =
            "/api/authentication/refresh_token/timeout/@time_unit";
    public static final String XML_PATH_TO_LOGIN_MAX_ATTEMPTS = "/api/authentication/login/max_attempts";
    public static final String XML_PATH_TO_LOGIN_LOCKOUT_MINUTES = "/api/authentication/login/lockout_minutes";
    public static final String XML_PATH_TO_AUTH_MFA_IP_CHECK = "/api/authentication/mfa_ip_check/@enabled";
    public static final String XML_PATH_TO_PASSWORD_RESET_WEB_ORIGIN = "/api/password_reset/web_origin";
    public static final String XML_PATH_TO_PASSWORD_RESET_TTL_MINUTES = "/api/password_reset/ttl_minutes";

    public static final String XML_PATH_TO_UPLOAD_DIR = "/api/storage/upload_dir";

    public static final String XML_PATH_TO_EMAIL_HOST = "/api/email/host";
    public static final String XML_PATH_TO_EMAIL_PORT = "/api/email/port";
    public static final String XML_PATH_TO_EMAIL_USER = "/api/email/username";
    public static final String XML_PATH_TO_EMAIL_PASS = "/api/email/password";
    public static final String XML_PATH_TO_EMAIL_FROM = "/api/email/from";
    public static final String XML_PATH_TO_EMAIL_STARTTLS = "/api/email/starttls";
    public static final String XML_PATH_TO_EMAIL_ENABLED = "/api/email/enabled";

    public static final String XML_PATH_TO_OTP_TTL_MINUTES = "/api/otp/ttl_minutes";
    public static final String XML_PATH_TO_OTP_MAX_ATTEMPTS = "/api/otp/max_attempts";
    public static final String XML_PATH_TO_OTP_LOCKOUT_MINUTES = "/api/otp/lockout_minutes";
    public static final String XML_PATH_TO_OTP_RESEND_COOLDOWN = "/api/otp/resend_cooldown_seconds";
    public static final String XML_PATH_TO_OTP_MAX_RESENDS = "/api/otp/max_resends";
    public static final String XML_PATH_TO_SCHEDULER_ENABLED = "/api/scheduler/enabled";
    public static final String XML_PATH_TO_SCHEDULER_THREAD_POOL_SIZE = "/api/scheduler/thread_pool_size";
    public static String XML_PATH_TO_CORS_ALLOWED_ORIGINS = "/api/cors/allowed_origins";
    public static String XML_PATH_TO_CORS_ALLOWED_METHODS = "/api/cors/allowed_methods";
    public static String XML_PATH_TO_CORS_ALLOWED_HEADERS = "/api/cors/allowed_headers";
    public static String XML_PATH_TO_CORS_EXPOSED_HEADERS = "/api/cors/exposed_headers";
    public static String XML_PATH_TO_CORS_ALLOW_CREDENTIALS = "/api/cors/allow_credentials";
    public static String XML_PATH_TO_CORS_MAX_AGE_SECONDS = "/api/cors/max_age_seconds";

    public static boolean dumpRequest() {
        return XmlUtils.readXMLTag(XML_PATH_TO_API_REQUEST_DUMP).equalsIgnoreCase("true");
    }

    public static String getApiContextPath() {
        return XmlUtils.readXMLTag(XML_PATH_TO_CONTEXT_PATH);
    }

    public static String getApiContextHost() {
        return XmlUtils.readXMLTag(XML_PATH_TO_CONTEXT_HOST);
    }

    public static Integer getApiContextPort() {
        return Integer.parseInt(XmlUtils.readXMLTag(XML_PATH_TO_CONTEXT_PORT));
    }

    public static int getIoThreadPool() {
        return Integer.parseInt(XmlUtils.readXMLTag(XML_PATH_TO_IO_THREAD_POOL));
    }

    public static int getWorkerThreadPool() {
        return Integer.parseInt(XmlUtils.readXMLTag(XML_PATH_TO_WORKER_THREAD_POOL));
    }

    public static String getDbHost() {
        return XmlUtils.readXMLTag(XML_PATH_TO_DB_HOST);
    }

    public static int getDbPort() {
        return Integer.parseInt(XmlUtils.readXMLTag(XML_PATH_TO_DB_PORT));
    }

    public static String getDbName() {
        return XmlUtils.readXMLTag(XML_PATH_TO_DB_NAME);
    }

    public static String getDbUser() {
        return CredentialVault.resolvePlaintext(XML_PATH_TO_DB_USER);
    }

    public static String getDbPass() {
        return CredentialVault.resolvePlaintext(XML_PATH_TO_DB_PASS);
    }

    public static int getDbPoolMin() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_DB_POOL_MIN);
        return (val == null || val.isBlank()) ? 5 : Integer.parseInt(val);
    }

    public static int getDbPoolMax() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_DB_POOL_MAX);
        return (val == null || val.isBlank()) ? 20 : Integer.parseInt(val);
    }

    public static int getDbCheckoutTimeoutSeconds() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_DB_TIMEOUT);
        return (val == null || val.isBlank()) ? 30 : Integer.parseInt(val);
    }

    public static boolean allowMultipleSessions() {
        return XmlUtils.readXMLTag(XML_PATH_TO_AUTH_MULTIPLE_SESSIONS).equalsIgnoreCase("true");
    }

    public static int getLoginMaxAttempts() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_LOGIN_MAX_ATTEMPTS);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 5;
        }
    }

    public static int getLoginLockoutMinutes() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_LOGIN_LOCKOUT_MINUTES);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 15;
        }
    }

    public static int getAccessTokenLength() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_AUTH_TOKEN_LENGTH);
        return (val == null || val.isBlank()) ? 80 : Integer.parseInt(val);
    }

    public static int getAccessTokenTimeout() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_AUTH_TOKEN_TIMEOUT);
        return (val == null || val.isBlank()) ? 30 : Integer.parseInt(val);
    }

    public static String getAccessTokenTimeoutUnit() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_AUTH_TOKEN_TIMEOUT_UNIT);
        return (val == null || val.isBlank()) ? "minutes" : val;
    }

    public static int getRefreshTokenLength() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_REFRESH_TOKEN_LENGTH);
        return (val == null || val.isBlank()) ? 80 : Integer.parseInt(val);
    }

    public static int getRefreshTokenTimeout() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_REFRESH_TOKEN_TIMEOUT);
        return (val == null || val.isBlank()) ? 30 : Integer.parseInt(val);
    }

    public static String getRefreshTokenTimeoutUnit() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_REFRESH_TOKEN_TIMEOUT_UNIT);
        return (val == null || val.isBlank()) ? "days" : val;
    }

    public static String getUploadDir() {
        String val = XmlUtils.readXMLTag(XML_PATH_TO_UPLOAD_DIR);
        if (val == null || val.isBlank()) val = "uploads";
        Path p = Paths.get(val);
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(p);
        }
        return p.toString();
    }

    public static String getEmailHost() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_EMAIL_HOST);
        return v == null || v.isBlank() ? "smtp.gmail.com" : v;
    }

    public static int getEmailPort() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_EMAIL_PORT);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 587;
        }
    }

    public static String getEmailUser() {
        return XmlUtils.readXMLTag(XML_PATH_TO_EMAIL_USER);
    }

    public static String getEmailPass() {
        return CredentialVault.resolvePlaintext(XML_PATH_TO_EMAIL_PASS);
    }

    public static String getEmailFrom() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_EMAIL_FROM);
        return v == null || v.isBlank() ? "Sky-Core <no-reply@skycore.com>" : v;
    }

    public static boolean isEmailStartTls() {
        return !"false".equalsIgnoreCase(XmlUtils.readXMLTag(XML_PATH_TO_EMAIL_STARTTLS));
    }

    public static boolean isEmailEnabled() {
        return "true".equalsIgnoreCase(XmlUtils.readXMLTag(XML_PATH_TO_EMAIL_ENABLED));
    }

    public static boolean isMfaIpCheckEnabled() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_AUTH_MFA_IP_CHECK);
        return !"false".equalsIgnoreCase(v); // default true
    }

    public static int getOtpTtlMinutes() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_OTP_TTL_MINUTES);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 15;
        }
    }

    public static int getOtpMaxAttempts() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_OTP_MAX_ATTEMPTS);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 5;
        }
    }

    public static int getOtpLockoutMinutes() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_OTP_LOCKOUT_MINUTES);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 60;
        }
    }

    public static int getOtpResendCooldownSeconds() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_OTP_RESEND_COOLDOWN);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 60;
        }
    }

    public static int getOtpMaxResends() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_OTP_MAX_RESENDS);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 3;
        }
    }

    public static int getSchedulerThreadPoolSize() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_SCHEDULER_THREAD_POOL_SIZE);

        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 5;
        }
    }

    public static boolean isSchedulerEnabled() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_SCHEDULER_ENABLED);
        return !"false".equalsIgnoreCase(v);
    }

    public static String getLogLevel() {
        String level = XmlUtils.readXMLTag(XML_PATH_TO_LOG_LEVEL);
        return (level == null || level.isBlank())
                ? "INFO"
                : level.toUpperCase();
    }

    public static String getPasswordResetWebOrigin() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_PASSWORD_RESET_WEB_ORIGIN);
        return (v == null || v.isBlank()) ? "http://localhost:5173" : v;
    }

    public static int getPasswordResetTtlMinutes() {
        String v = XmlUtils.readXMLTag(XML_PATH_TO_PASSWORD_RESET_TTL_MINUTES);
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 30;
        }
    }

    public static String[] getCorsAllowedOrigins() {
        List<String> origins = XmlUtils.readXMLTags(XML_PATH_TO_CORS_ALLOWED_ORIGINS, "origin");
        return origins.toArray(new String[0]);
    }

    public static String getCorsAllowedMethods() {
        return XmlUtils.readXMLTag(XML_PATH_TO_CORS_ALLOWED_METHODS);
    }

    public static String getCorsAllowedHeaders() {
        return XmlUtils.readXMLTag(XML_PATH_TO_CORS_ALLOWED_HEADERS);
    }

    public static String getCorsExposedHeaders() {
        return XmlUtils.readXMLTag(XML_PATH_TO_CORS_EXPOSED_HEADERS);
    }

    public static boolean getCorsAllowCredentials() {
        return Boolean.parseBoolean(XmlUtils.readXMLTag(XML_PATH_TO_CORS_ALLOW_CREDENTIALS));
    }

    public static int getCorsMaxAgeSeconds() {
        return Integer.parseInt(XmlUtils.readXMLTag(XML_PATH_TO_CORS_MAX_AGE_SECONDS));
    }


    public static class Schema {
        public static final String MASTER = "master";
        public static final String IDENTITY = "identity";
        public static final String SAVINGS = "savings";
        public static final String SHARES = "shares";
        public static final String LENDING = "lending";
        public static final String LEDGER = "ledger";
        public static final String ACCOUNTING = "accounting";
        public static final String TREASURY = "treasury";
        public static final String RISK = "risk";
        public static final String DOCUMENTS = "documents";
        public static final String SCHEDULING = "scheduling";
        public static final String AUDIT = "audit";
        public static final String REFERENCE = "reference";
        public static final String CONFIGURATION = "configuration";
    }

    public static class Table {
        public static final String CURRENCIES = Schema.REFERENCE + ".currencies";
        public static final String COUNTRIES = Schema.REFERENCE + ".countries";
        public static final String PAYMENT_METHODS = Schema.REFERENCE + ".payment_methods";
        public static final String TRANSACTION_TYPES = Schema.REFERENCE + ".transaction_types";
        public static final String FEE_TYPES = Schema.REFERENCE + ".fee_types";

        public static final String SYSTEM_SETTINGS = Schema.CONFIGURATION + ".system_settings";
        public static final String ACCOUNT_NUMBER_SEQUENCES = Schema.CONFIGURATION + ".account_number_sequences";

        public static final String USER_ACCOUNTS = Schema.IDENTITY + ".user_accounts";
        public static final String ROLES = Schema.IDENTITY + ".roles";
        public static final String PERMISSIONS = Schema.IDENTITY + ".permissions";
        public static final String ROLE_PERMISSIONS = Schema.IDENTITY + ".role_permissions";
        public static final String USER_ROLES = Schema.IDENTITY + ".user_roles";
        public static final String TOKENS = Schema.IDENTITY + ".tokens";
        public static final String OTP_CODES = Schema.IDENTITY + ".otp_codes";
        public static final String AUTHENTICATION_EVENTS = Schema.IDENTITY + ".authentication_events";

        public static final String MEMBERS = Schema.MASTER + ".members";
        public static final String MEMBER_CONTACTS = Schema.MASTER + ".member_contacts";
        public static final String MEMBER_ADDRESSES = Schema.MASTER + ".member_addresses";
        public static final String MEMBER_STATUS_HISTORY = Schema.MASTER + ".member_status_history";
        public static final String MEMBER_PAYOUT_CHANNELS = Schema.MASTER + ".member_payout_channels";

        public static final String SAVINGS_PRODUCTS = Schema.SAVINGS + ".savings_products";
        public static final String SAVINGS_ACCOUNTS = Schema.SAVINGS + ".savings_accounts";
        public static final String SAVINGS_ACCOUNT_STATUS_HISTORY = Schema.SAVINGS + ".savings_account_status_history";
        public static final String SAVINGS_INTEREST_POSTINGS = Schema.SAVINGS + ".savings_interest_postings";

        public static final String SHARE_CLASSES = Schema.SHARES + ".share_classes";
        public static final String MEMBER_SHARES = Schema.SHARES + ".member_shares";
        public static final String SHARE_TRANSACTIONS = Schema.SHARES + ".share_transactions";

        public static final String LOAN_PRODUCTS = Schema.LENDING + ".loan_products";
        public static final String LOANS = Schema.LENDING + ".loans";
        public static final String LOAN_GUARANTORS = Schema.LENDING + ".loan_guarantors";
        public static final String LOAN_PRODUCT_FEES = Schema.LENDING + ".loan_product_fees";
        public static final String LOAN_FEE_CHARGES = Schema.LENDING + ".loan_fee_charges";
        public static final String LOAN_SCHEDULES = Schema.LENDING + ".loan_schedules";
        public static final String LOAN_INSTALLMENTS = Schema.LENDING + ".loan_installments";
        public static final String LOAN_STATUS_HISTORY = Schema.LENDING + ".loan_status_history";
        public static final String LOAN_REPAYMENTS = Schema.LENDING + ".loan_repayments";
        public static final String LOAN_REPAYMENT_ALLOCATIONS = Schema.LENDING + ".loan_repayment_allocations";
        public static final String LOAN_PENALTIES = Schema.LENDING + ".loan_penalties";
        public static final String LOAN_INTEREST_ACCRUALS = Schema.LENDING + ".loan_interest_accruals";
        public static final String LOAN_SETTLEMENTS = Schema.LENDING + ".loan_settlements";

        public static final String TRANSACTIONS = Schema.LEDGER + ".transactions";
        public static final String TRANSACTION_LINES = Schema.LEDGER + ".transaction_lines";
        public static final String TRANSACTION_REVERSALS = Schema.LEDGER + ".transaction_reversals";

        public static final String CHART_OF_ACCOUNTS = Schema.ACCOUNTING + ".chart_of_accounts";
        public static final String ACCOUNTING_PERIODS = Schema.ACCOUNTING + ".accounting_periods";
        public static final String JOURNAL_ENTRIES = Schema.ACCOUNTING + ".journal_entries";
        public static final String JOURNAL_ENTRY_LINES = Schema.ACCOUNTING + ".journal_entry_lines";
        public static final String DIVIDEND_DECLARATIONS = Schema.ACCOUNTING + ".dividend_declarations";
        public static final String DIVIDEND_DISBURSEMENTS = Schema.ACCOUNTING + ".dividend_disbursements";

        public static final String TREASURY_ACCOUNTS = Schema.TREASURY + ".treasury_accounts";
        public static final String PESA_IN = Schema.TREASURY + ".pesa_in";
        public static final String PESA_OUT = Schema.TREASURY + ".pesa_out";

        public static final String LOAN_RISK_CLASSIFICATIONS = Schema.RISK + ".loan_risk_classifications";
        public static final String IMPAIRMENT_ASSESSMENTS = Schema.RISK + ".impairment_assessments";
        public static final String EXPECTED_CREDIT_LOSSES = Schema.RISK + ".expected_credit_losses";
        public static final String WRITE_OFFS = Schema.RISK + ".write_offs";

        public static final String FILE_UPLOADS = Schema.DOCUMENTS + ".file_uploads";
        public static final String DOCUMENT_LINKS = Schema.DOCUMENTS + ".document_links";

        public static final String QRTZ_JOB_DETAILS = Schema.SCHEDULING + ".qrtz_job_details";
        public static final String QRTZ_TRIGGERS = Schema.SCHEDULING + ".qrtz_triggers";
        public static final String QRTZ_SIMPLE_TRIGGERS = Schema.SCHEDULING + ".qrtz_simple_triggers";
        public static final String QRTZ_CRON_TRIGGERS = Schema.SCHEDULING + ".qrtz_cron_triggers";
        public static final String QRTZ_SIMPROP_TRIGGERS = Schema.SCHEDULING + ".qrtz_simprop_triggers";
        public static final String QRTZ_BLOB_TRIGGERS = Schema.SCHEDULING + ".qrtz_blob_triggers";
        public static final String QRTZ_CALENDARS = Schema.SCHEDULING + ".qrtz_calendars";
        public static final String QRTZ_PAUSED_TRIGGER_GRPS = Schema.SCHEDULING + ".qrtz_paused_trigger_grps";
        public static final String QRTZ_FIRED_TRIGGERS = Schema.SCHEDULING + ".qrtz_fired_triggers";
        public static final String QRTZ_SCHEDULER_STATE = Schema.SCHEDULING + ".qrtz_scheduler_state";
        public static final String QRTZ_LOCKS = Schema.SCHEDULING + ".qrtz_locks";

        public static final String AUDIT_EVENTS = Schema.AUDIT + ".audit_events";
        public static final String DATA_CHANGE_EVENTS = Schema.AUDIT + ".data_change_events";

        private Table() {
        }
    }

}
