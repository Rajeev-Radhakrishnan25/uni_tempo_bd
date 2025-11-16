package com.asdc.unicarpool.constant;

public class AppConstant {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final class JWT {
        public static final String BANNER_ID = "bannerId";
        public static final String EMAIL = "email";
        public static final String ROLES = "roles";
        public static final String NAME = "name";

        private JWT() {
        }
    }

    public static final class Headers {
        public static final String AUTHORIZATION = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
    }

    public static final class ErrorResponse {
        public static final String TIMESTAMP = "timestamp";
        public static final String STATUS = "status";
        public static final String MESSAGE = "message";
    }

    public static final class EmailConstant {
        public static class Templates {
            public static final String VERIFICATION_EMAIL_TEMPLATE = "verification-code";
            public static final String FORGET_PASSWORD_TEMPLATE = "forget-password";
            public static final String RIDE_REQUEST_CHANGE_TEMPLATE = "ride-request-status-update";
        }

        public static class EmailSubjects {
            public static final String VERIFICATION_SUBJECT = "Unicarpool Verification Request";
            public static final String FORGET_PASSWORD_SUBJECT = "Unicarpool Forget Password Request";
        }
    }

    public enum VerificationStatus {
        ACTIVE,
        EXPIRED,
        USED;
    }

    public enum VerificationType {
        EMAIL,
        FORGET_PASSWORD
    }
}
