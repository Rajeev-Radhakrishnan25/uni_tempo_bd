package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.VerificationCode;
import com.asdc.unicarpool.repository.IVerificationCodeRepository;
import com.asdc.unicarpool.service.IVerificationService;
import com.asdc.unicarpool.util.EmailUtil.EmailUtil;
import com.asdc.unicarpool.util.EmailUtil.IEmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class VerificationService implements IVerificationService {

    @Value("${spring.application.name}")
    private String appName;

    private final IVerificationCodeRepository verificationCodeRepository;
    private final IEmailUtil emailUtil;

    private final Integer EXPIRY_MINUTES = 15;
    private final Integer VERIFICATION_CODE_SIZE = 6;

    @Autowired
    public VerificationService(EmailUtil emailUtil,
                               IVerificationCodeRepository verificationCodeRepository
    ) {
        this.emailUtil = emailUtil;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @Override
    public Boolean sendVerificationCode(User user, AppConstant.VerificationType type) {
        log.debug("Banner Id: {}", user.getBannerId());

        Optional<VerificationCode> verificationCode = verificationCodeRepository.findByUserIdAndStatusAndType(user, AppConstant.VerificationStatus.ACTIVE.name(), type.name());

        if (verificationCode.isPresent()) {
            verificationCode.get().setStatus(AppConstant.VerificationStatus.EXPIRED.name());
            verificationCodeRepository.save(verificationCode.get());
        }

        Integer code = generateVerificationCode();
        Instant expiryTime = Instant.now().plusSeconds(EXPIRY_MINUTES * 60);

        log.debug("Code Expiry: {}", expiryTime);
        VerificationCode newVerificationCode = VerificationCode.builder()
                .code(code)
                .userId(user)
                .expireAt(expiryTime)
                .type(type.name())
                .build();

        log.debug("Verification Obj {}", newVerificationCode);

        verificationCodeRepository.save(newVerificationCode);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("appName", appName);
        templateData.put("user", user.getName());
        templateData.put("verificationCode", code);

        String template = type == AppConstant.VerificationType.EMAIL ? AppConstant.EmailConstant.Templates.VERIFICATION_EMAIL_TEMPLATE : AppConstant.EmailConstant.Templates.FORGET_PASSWORD_TEMPLATE;
        String subject = type == AppConstant.VerificationType.EMAIL ? AppConstant.EmailConstant.EmailSubjects.VERIFICATION_SUBJECT : AppConstant.EmailConstant.EmailSubjects.FORGET_PASSWORD_SUBJECT;
        emailUtil.sendEmail(user.getEmail(), subject, template, templateData);

        return true;
    }

    @Override
    public Boolean verifyCode(User user, Integer code, AppConstant.VerificationType type) {
        log.debug("Banner Id: {}", user.getBannerId());
        log.debug("Verification code: {}", code);

        VerificationCode verificationCode = verificationCodeRepository.findByUserIdAndStatusAndType(user, AppConstant.VerificationStatus.ACTIVE.name(), type.name()).orElseThrow(() -> new InvalidArgumentException("Invalid Verification Code"));

        if (!Objects.equals(verificationCode.getCode(), code)) {
            throw new InvalidArgumentException("Invalid Verification Code");
        } else if (verificationCode.getExpireAt().isBefore(Instant.now())) {
            verificationCode.setStatus(AppConstant.VerificationStatus.EXPIRED.name());
            verificationCodeRepository.save(verificationCode);
            throw new InvalidArgumentException("Verification Code has expired");
        } else {
            verificationCode.setStatus(AppConstant.VerificationStatus.USED.name());
            verificationCodeRepository.save(verificationCode);
            return true;
        }
    }

    private Integer generateVerificationCode() {
        Random random = new Random();
        int code = 0;
        for (int i = 0; i < VERIFICATION_CODE_SIZE; i++) {
            code = (code * 10) + random.nextInt(10);
        }
        return code;
    }
}
