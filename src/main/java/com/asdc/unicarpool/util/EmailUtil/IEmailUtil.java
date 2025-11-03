package com.asdc.unicarpool.util.EmailUtil;

import java.util.Map;

public interface IEmailUtil {
    /**
     * Utility for sending email
     *
     * @param recipient
     * @param subject
     * @param templateName
     * @param templateData
     */
    public void sendEmail(String recipient, String subject, String templateName, Map<String, Object> templateData);

}
