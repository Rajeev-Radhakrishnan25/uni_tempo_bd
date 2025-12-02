package com.asdc.unicarpool.util.EmailUtil;

import com.asdc.unicarpool.exception.EmailSenderException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailUtilTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailUtil emailUtil;

    private static final String SENDER_EMAIL = "test@unicarpool.com";
    private static final String RECIPIENT_EMAIL = "recipient@test.com";
    private static final String SUBJECT = "Test Subject";
    private static final String TEMPLATE_NAME = "test-template";
    private static final String PROCESSED_BODY = "<html><body>Test Email</body></html>";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailUtil, "senderEmail", SENDER_EMAIL);
    }

    @Test
    void testSendEmail_Success() {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", "John Doe");
        templateData.put("code", "123456");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEMPLATE_NAME), any(Context.class))).thenReturn(PROCESSED_BODY);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailUtil.sendEmail(RECIPIENT_EMAIL, SUBJECT, TEMPLATE_NAME, templateData));

        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq(TEMPLATE_NAME), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendEmail_WithEmptyTemplateData() {
        Map<String, Object> templateData = new HashMap<>();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEMPLATE_NAME), any(Context.class))).thenReturn(PROCESSED_BODY);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailUtil.sendEmail(RECIPIENT_EMAIL, SUBJECT, TEMPLATE_NAME, templateData));

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendEmail_ThrowsEmailSenderException_WhenMessagingExceptionOccurs() {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", "John Doe");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEMPLATE_NAME), any(Context.class)))
                .thenThrow(new RuntimeException("Template processing failed"));

        EmailSenderException exception = assertThrows(EmailSenderException.class,
                () -> emailUtil.sendEmail(RECIPIENT_EMAIL, SUBJECT, TEMPLATE_NAME, templateData));

        assertNotNull(exception.getMessage());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmail_ThrowsEmailSenderException_WhenMailSenderFails() {
        Map<String, Object> templateData = new HashMap<>();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEMPLATE_NAME), any(Context.class))).thenReturn(PROCESSED_BODY);
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        EmailSenderException exception = assertThrows(EmailSenderException.class,
                () -> emailUtil.sendEmail(RECIPIENT_EMAIL, SUBJECT, TEMPLATE_NAME, templateData));

        assertNotNull(exception.getMessage());
        verify(mailSender, times(1)).createMimeMessage();
    }

    @Test
    void testSendEmail_WithMultipleTemplateVariables() {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("userName", "Jane Smith");
        templateData.put("verificationCode", "987654");
        templateData.put("expiryTime", "15 minutes");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(TEMPLATE_NAME), any(Context.class))).thenReturn(PROCESSED_BODY);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailUtil.sendEmail(RECIPIENT_EMAIL, SUBJECT, TEMPLATE_NAME, templateData));

        verify(templateEngine, times(1)).process(eq(TEMPLATE_NAME), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }
}
