package com.asdc.unicarpool.service.service;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.dto.request.LoginRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;
import com.asdc.unicarpool.dto.response.LoginResponse;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.IUserService;
import com.asdc.unicarpool.service.impl.AuthService;
import com.asdc.unicarpool.service.impl.VerificationService;
import com.asdc.unicarpool.util.TokenUtil.JwtUtil;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private IUserRepository  userRepository;
    private VerificationService verificationService;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthService authService;

    private User testUser;


    @BeforeEach
    void setUp() {
        userRepository = mock(IUserRepository.class);
        verificationService = mock(VerificationService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtUtil = mock(JwtUtil.class);

        authService = new AuthService(userRepository,passwordEncoder,jwtUtil,null,verificationService);

        testUser = User.builder()
                .id(1L)
                .bannerId("B00875982")
                .password("Pass@123")
                .email("mostafaa@dal.ca")
                .emailVerified(true)
                .build();
    }


    @Test
    void authenticateUser_success(){
        LoginRequest request = new LoginRequest("B00875982","Pass@123");
        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Pass@123",testUser.getPassword())).thenReturn(true);

        when(jwtUtil.generateToken(testUser)).thenReturn("JWT123");
        when(jwtUtil.getExpirationTime()).thenReturn(3600L);

        LoginResponse response = authService.authenticateUser(request);

        assertEquals("JWT123", response.getToken());
        assertEquals(3600L, response.getExpiresIn());

        verify(userRepository).findByBannerId("B00875982");
    }

    @Test
    void authenticateUser_invalidPasswordException(){
        LoginRequest request = new LoginRequest("B00875982","pass");

        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Pass@123",testUser.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,()-> authService.authenticateUser(request));

    }

    @Test
    void authenticateUser_notVerifiedException(){

        testUser.setEmailVerified(false);

        LoginRequest request = new LoginRequest("B00875982","Pass@123");
        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Pass@123",testUser.getPassword())).thenReturn(true);

        assertThrows(InvalidCredentialsException.class,()-> authService.authenticateUser(request));
    }

    @Test
    void resetPassword_sucess(){
        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPass@123")).thenReturn("NewEncodedPass@123");

        BaseResponse response = authService.resetPassword("B00875982","NewPass@123");

        assertEquals("Password reset successfully!",response.getMessage());
        assertEquals("NewEncodedPass@123", testUser.getPassword());
        verify(userRepository).findByBannerId("B00875982");

    }

    @Test
    void sendEmailVerificationCode_success(){
        testUser.setEmailVerified(false);

        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));

        BaseResponse response = authService.sendEmailVerificationCode("B00875982");
        assertEquals("Verification Code has been sent successfully",response.getMessage());
        verify(verificationService).sendVerificationCode(testUser, AppConstant.VerificationType.EMAIL);
    }

    @Test
    void sendEmailVerificationCode_alreadyVerified(){
        testUser.setEmailVerified(true);

        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));

        assertThrows(InvalidArgumentException.class,()-> authService.sendEmailVerificationCode("B00875982"));
    }

    @Test
    void verifyEmailVerificationCode_success(){
        testUser.setEmailVerified(false);

        when(userRepository.findByBannerIdAndEmailVerifiedIsFalse("B00875982"))
                .thenReturn(Optional.of(testUser));

        BaseResponse response = authService.verifyEmail("B00875982",123456);

        assertEquals("Verification successful",response.getMessage());
        assertTrue(testUser.isEmailVerified());

        verify(verificationService).verifyCode(testUser,123456, AppConstant.VerificationType.EMAIL);

        verify(userRepository).save(testUser);
    }

    @Test
    void verifyEmailVerificationCode_invalidCode(){
        testUser.setEmailVerified(true);

        when(userRepository.findByBannerIdAndEmailVerifiedIsFalse("B00875982"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,()-> authService.verifyEmail("B00875982",10000));

        verify(verificationService,never()).verifyCode(any(),any(),any());

        verify(userRepository,never()).save(any());
    }

    @Test
    void sendForgotPassword_success(){
        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));

        BaseResponse response = authService.sendForgetPasswordCode("B00875982");
        assertEquals("Verification Code has been sent successfully",response.getMessage());

        verify(verificationService).sendVerificationCode(testUser, AppConstant.VerificationType.FORGET_PASSWORD);
    }

    @Test
    void recoveryPassword_success(){
        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));

        when(passwordEncoder.encode("NewPass@123")).thenReturn("NewEncodedPass@123");

        BaseResponse response = authService.recoverPassword("B00875982",123456,"NewPass@123");

        assertEquals("Password recovered successfully",response.getMessage());
        assertEquals("NewEncodedPass@123", testUser.getPassword());

        verify(userRepository).save(testUser);
        verify(verificationService).verifyCode(testUser,123456,AppConstant.VerificationType.FORGET_PASSWORD);
    }

}