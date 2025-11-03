package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.dto.request.LoginRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;
import com.asdc.unicarpool.dto.response.LoginResponse;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.exception.ResourceNotFoundException;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.IAuthService;
import com.asdc.unicarpool.service.IVerificationService;
import com.asdc.unicarpool.util.EmailUtil.EmailUtil;
import com.asdc.unicarpool.util.TokenUtil.ITokenUtil;
import com.asdc.unicarpool.util.TokenUtil.JwtUtilI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService implements IAuthService {

    @Value("${spring.application.name}")
    private String appName;

    private final IUserRepository userRepository;
    private final IVerificationService verificationService;

    private final PasswordEncoder passwordEncoder;
    private final ITokenUtil jwtUtil;

    @Autowired
    public AuthService(IUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtilI jwtUtil,
                       EmailUtil emailUtil,
                       VerificationService verificationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.verificationService = verificationService;
    }

    @Override
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        log.debug("Login Request {}", loginRequest);
        User user = userRepository.findByBannerId(loginRequest.getBannerId()).orElseThrow(() -> new InvalidCredentialsException("User not found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        if (!user.isEmailVerified()) throw new InvalidCredentialsException("User is not verified");

        String token = jwtUtil.generateToken(user);
        Long expiration = jwtUtil.getExpirationTime();

        return LoginResponse.builder().token(token).expiresIn(expiration).build();
    }

    @Override
    public BaseResponse resetPassword(String bannerId, String password) {
        User user = userRepository.findByBannerId(bannerId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return new BaseResponse("Password reset successfully!");
    }

    @Override
    public BaseResponse sendEmailVerificationCode(String bannerId) {
        log.debug("Banner Id: {}", bannerId);

        User user = userRepository.findByBannerId(bannerId).orElseThrow(() -> new InvalidCredentialsException("User not found"));
        if (user.isEmailVerified()) throw new InvalidArgumentException("User is already verified");
        verificationService.sendVerificationCode(user, AppConstant.VerificationType.EMAIL);
        return new BaseResponse("Verification Code has been sent successfully");
    }

    @Override
    public BaseResponse verifyEmail(String bannerId, Integer code) {
        log.debug("Banner Id: {}", bannerId);
        log.debug("Verification code: {}", code);
        User user = userRepository.findByBannerIdAndEmailVerifiedIsFalse(bannerId).orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (user.isEmailVerified()) throw new InvalidArgumentException("User is already verified");

        verificationService.verifyCode(user, code, AppConstant.VerificationType.EMAIL);

        user.setEmailVerified(true);
        userRepository.save(user);

        return new BaseResponse("Verification successful");
    }

    @Override
    public BaseResponse sendForgetPasswordCode(String bannerId) {
        log.debug("Banner Id: {}", bannerId);

        User user = userRepository.findByBannerId(bannerId).orElseThrow(() -> new InvalidCredentialsException("User not found"));

        verificationService.sendVerificationCode(user, AppConstant.VerificationType.FORGET_PASSWORD);
        return new BaseResponse("Verification Code has been sent successfully");
    }

    @Override
    public BaseResponse recoverPassword(String bannerId, Integer code, String password) {
        log.debug("Banner Id: {}", bannerId);
        log.debug("code Id: {}", code);
        log.debug("password Id: {}", password);

        User user = userRepository.findByBannerId(bannerId).orElseThrow(() -> new InvalidCredentialsException("User not found"));

        verificationService.verifyCode(user, code, AppConstant.VerificationType.FORGET_PASSWORD);

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return new BaseResponse("Password recovered successfully");
    }
}
