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
        public static final String PUBLIC = "public";
    }

    public static class Table {
        public static final String ACCESS_TOKENS = Schema.PUBLIC + ".access_tokens";
        public static final String ADJUSTMENT_REASON_CODES = Schema.PUBLIC + ".adjustment_reason_codes";
        public static final String ADVANCE_SHIPPING_NOTICE_LINES = Schema.PUBLIC + ".advance_shipping_notice_lines";
        public static final String ADVANCE_SHIPPING_NOTICES = Schema.PUBLIC + ".advance_shipping_notices";
        public static final String BACKORDERS = Schema.PUBLIC + ".backorders";
        public static final String CUSTOMERS = Schema.PUBLIC + ".customers";
        public static final String CYCLE_COUNT_RESULT_LINES = Schema.PUBLIC + ".cycle_count_result_lines";
        public static final String CYCLE_COUNT_SCHEDULES = Schema.PUBLIC + ".cycle_count_schedules";
        public static final String CYCLE_COUNT_TASK_LINES = Schema.PUBLIC + ".cycle_count_task_lines";
        public static final String CYCLE_COUNT_TASKS = Schema.PUBLIC + ".cycle_count_tasks";
        public static final String CYCLE_COUNT_VARIANCE_REVIEWS = Schema.PUBLIC + ".cycle_count_variance_reviews";
        public static final String DOCK_APPOINTMENTS = Schema.PUBLIC + ".dock_appointments";
        public static final String GOODS_RECEIPT_DISCREPANCIES = Schema.PUBLIC + ".goods_receipt_discrepancies";
        public static final String GOODS_RECEIPT_LINES = Schema.PUBLIC + ".goods_receipt_lines";
        public static final String GOODS_RECEIPTS = Schema.PUBLIC + ".goods_receipts";
        public static final String INTERNAL_TRANSFER_LINES = Schema.PUBLIC + ".internal_transfer_lines";
        public static final String INTERNAL_TRANSFER_REQUESTS = Schema.PUBLIC + ".internal_transfer_requests";
        public static final String INVENTORY_ADJUSTMENT_LINES = Schema.PUBLIC + ".inventory_adjustment_lines";
        public static final String INVENTORY_ADJUSTMENTS = Schema.PUBLIC + ".inventory_adjustments";
        public static final String INVOICE_LINES = Schema.PUBLIC + ".invoice_lines";
        public static final String INVOICES = Schema.PUBLIC + ".invoices";
        public static final String LICENSE_PLATES = Schema.PUBLIC + ".license_plates";
        public static final String ORDER_LINES = Schema.PUBLIC + ".order_lines";
        public static final String ORDERS = Schema.PUBLIC + ".orders";
        public static final String PACKAGE_LINES = Schema.PUBLIC + ".package_lines";
        public static final String PACKAGES = Schema.PUBLIC + ".packages";
        public static final String PAYMENT_APPLICATIONS = Schema.PUBLIC + ".payment_applications";
        public static final String PAYMENTS = Schema.PUBLIC + ".payments";
        public static final String PERMISSIONS = Schema.PUBLIC + ".permissions";
        public static final String PICK_CONFIRMATIONS = Schema.PUBLIC + ".pick_confirmations";
        public static final String PICK_TASKS = Schema.PUBLIC + ".pick_tasks";
        public static final String PICK_WAVE_ASSIGNMENTS = Schema.PUBLIC + ".pick_wave_assignments";
        public static final String PICK_WAVES = Schema.PUBLIC + ".pick_waves";
        public static final String PRODUCTS = Schema.PUBLIC + ".products";
        public static final String PURCHASE_ORDER_LINES = Schema.PUBLIC + ".purchase_order_lines";
        public static final String PURCHASE_ORDERS = Schema.PUBLIC + ".purchase_orders";
        public static final String PUTAWAY_CONFIRMATIONS = Schema.PUBLIC + ".putaway_confirmations";
        public static final String PUTAWAY_TASKS = Schema.PUBLIC + ".putaway_tasks";
        public static final String QUALITY_ASSURANCE_INSPECTION_RESULTS = Schema.PUBLIC + ".quality_assurance_inspection_results";
        public static final String QUALITY_ASSURANCE_INSPECTIONS = Schema.PUBLIC + ".quality_assurance_inspections";
        public static final String REPLENISHMENT_RULES = Schema.PUBLIC + ".replenishment_rules";
        public static final String RETURN_LINES = Schema.PUBLIC + ".return_lines";
        public static final String RETURNS = Schema.PUBLIC + ".returns";
        public static final String ROLE_PERMISSIONS = Schema.PUBLIC + ".role_permissions";
        public static final String ROLES = Schema.PUBLIC + ".roles";
        public static final String SHIPMENT_PACKAGES = Schema.PUBLIC + ".shipment_packages";
        public static final String SHIPMENTS = Schema.PUBLIC + ".shipments";
        public static final String STOCK_MOVEMENTS = Schema.PUBLIC + ".stock_movements";
        public static final String STOCK_RESERVATIONS = Schema.PUBLIC + ".stock_reservations";
        public static final String STORAGE_LOCATIONS = Schema.PUBLIC + ".storage_locations";
        public static final String SUPPLIERS = Schema.PUBLIC + ".suppliers";
        public static final String USER_ACCOUNTS = Schema.PUBLIC + ".user_accounts";
        public static final String USER_ROLES = Schema.PUBLIC + ".user_roles";
        public static final String WAREHOUSES = Schema.PUBLIC + ".warehouses";

        private Table() {
        }
}}
