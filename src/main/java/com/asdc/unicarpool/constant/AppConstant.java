package com.asdc.unicarpool.constant;

public class AppConstant {
    // Date/Time Format
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // JWT Constants
    public static final String JWT_BANNER_ID = "bannerId";
    public static final String JWT_EMAIL = "email";
    public static final String JWT_ROLES = "roles";
    public static final String JWT_NAME = "name";

    // HTTP Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_BEARER_PREFIX = "Bearer ";

    // Error Response Keys
    public static final String ERROR_TIMESTAMP = "timestamp";
    public static final String ERROR_STATUS = "status";
    public static final String ERROR_MESSAGE = "message";

    // Email Templates
    public static final String EMAIL_TEMPLATE_VERIFICATION = "verification-code";
    public static final String EMAIL_TEMPLATE_FORGET_PASSWORD = "forget-password";
    public static final String EMAIL_TEMPLATE_RIDE_REQUEST_CHANGE = "ride-request-status-update";
    public static final String EMAIL_TEMPLATE_RIDE_STATUS = "ride-status";

    // Email Subjects
    public static final String EMAIL_SUBJECT_VERIFICATION = "Unicarpool Verification Request";
    public static final String EMAIL_SUBJECT_FORGET_PASSWORD = "Unicarpool Forget Password Request";

    // Rating Constants
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    //Verification Code Constants
    public static final int VERIFICATION_CODE_LENGTH = 6;
    public static final long VERIFICATION_CODE_EXPIRY_MINS = 15L;

    // Time Conversion Constants
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final int SECONDS_PER_MINUTE = 60;

    // Numeric Constants
    public static final int DECIMAL_BASE = 10;

    // Enums
    public enum VerificationStatus {
        ACTIVE,
        EXPIRED,
        USED
    }

    public enum VerificationType {
        EMAIL,
        FORGET_PASSWORD
    }

    // Private constructor to prevent instantiation
    private AppConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
