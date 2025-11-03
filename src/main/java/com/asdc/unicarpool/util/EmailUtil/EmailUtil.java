package com.asdc.unicarpool.util.EmailUtil;

import com.asdc.unicarpool.exception.EmailSenderException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class EmailUtil implements IEmailUtil {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailUtil(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Value("${spring.application.appEmail}")
    private String senderEmail;

    public void sendEmail(String recipient, String subject, String templateName, Map<String, Object> templateData) {
        try {
            log.debug("Sending email to {}", recipient);
            log.debug("Subject: {}", templateName);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.displayName());

            helper.setFrom(senderEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariables(templateData);
            String body = templateEngine.process(templateName, context);

            helper.setText(body, true);
            mailSender.send(message);
            log.info("Email sent successfully");

        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new EmailSenderException(e.getMessage());
        } catch (Exception e) {
            log.error("Mail Util Exception: {}", e.getMessage());
            throw new EmailSenderException(e.getMessage());
        }
    }
}
