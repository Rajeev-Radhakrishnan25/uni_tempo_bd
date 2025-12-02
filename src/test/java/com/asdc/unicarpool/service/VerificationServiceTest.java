package com.asdc.unicarpool.service;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.VerificationCode;
import com.asdc.unicarpool.repository.IVerificationCodeRepository;
import com.asdc.unicarpool.service.impl.VerificationService;
import com.asdc.unicarpool.util.EmailUtil.IEmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VerificationServiceTest {

    private VerificationService verificationService;
    private IVerificationCodeRepository verificationCodeRepository;
    private IEmailUtil emailUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        verificationCodeRepository = mock(IVerificationCodeRepository.class);
        emailUtil = mock(com.asdc.unicarpool.util.EmailUtil.EmailUtil.class, withSettings().extraInterfaces(IEmailUtil.class));

        verificationService = new VerificationService((com.asdc.unicarpool.util.EmailUtil.EmailUtil) emailUtil, verificationCodeRepository);

        testUser = User.builder()
                .id(1L)
                .name("Mostafaa Abdelaziz")
                .email("mostafaa@dal.ca")
                .build();
    }


    @Test
    void sendVerificationCode_Success() {
        when(verificationCodeRepository.findByUserIdAndStatusAndType(
                eq(testUser), any(), any())).thenReturn(Optional.empty());

        boolean result = verificationService.sendVerificationCode(testUser, AppConstant.VerificationType.EMAIL);
        assertTrue(result);
        verify(verificationCodeRepository).save(any(VerificationCode.class));
        verify(emailUtil).sendEmail(eq(testUser.getEmail()), any(), any(), any());
    }

    @Test
    void verifyCode_Success() {
        VerificationCode code = VerificationCode.builder()
                .code(123456)
                .userId(testUser)
                .expireAt(Instant.now().plusSeconds(60))
                .type(AppConstant.VerificationType.EMAIL.name())
                .status(AppConstant.VerificationStatus.ACTIVE.name())
                .build();


        when(verificationCodeRepository.findByUserIdAndStatusAndType(
                eq(testUser), any(), any())).thenReturn(Optional.of(code));

        boolean result = verificationService.verifyCode(testUser, 123456, AppConstant.VerificationType.EMAIL);

        assertTrue(result);
        assertEquals(AppConstant.VerificationStatus.USED.name(), code.getStatus());
        verify(verificationCodeRepository).save(any(VerificationCode.class));
    }

    @Test
    void verifyCode_CodeNotFound_ThrowsException() {
        when(verificationCodeRepository.findByUserIdAndStatusAndType(
                eq(testUser), any(), any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> verificationService.verifyCode(testUser, 123456, AppConstant.VerificationType.EMAIL));
        verify(verificationCodeRepository).findByUserIdAndStatusAndType(eq(testUser), any(), any());
        verify(verificationCodeRepository, never()).save(any(VerificationCode.class));
    }

    @Test
    void verifyCode_CodeExpired_ThrowsException() {
        VerificationCode code = VerificationCode.builder()
                .code(123456)
                .userId(testUser)
                .expireAt(Instant.now().minusSeconds(60))
                .type(AppConstant.VerificationType.EMAIL.name())
                .status(AppConstant.VerificationStatus.ACTIVE.name())
                .build();

        when(verificationCodeRepository.findByUserIdAndStatusAndType(
                eq(testUser), any(), any())).thenReturn(Optional.of(code));

        assertThrows(InvalidArgumentException.class, () -> verificationService.verifyCode(testUser, 123456, AppConstant.VerificationType.EMAIL));
        assertEquals(AppConstant.VerificationStatus.EXPIRED.name(), code.getStatus());
        verify(verificationCodeRepository).save(code);
    }
}