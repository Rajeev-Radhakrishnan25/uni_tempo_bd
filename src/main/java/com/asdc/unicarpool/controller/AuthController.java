package com.asdc.unicarpool.controller;

import com.asdc.unicarpool.dto.request.LoginRequest;
import com.asdc.unicarpool.dto.request.ResetPasswordRequest;
import com.asdc.unicarpool.dto.request.VerificationCodeRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;
import com.asdc.unicarpool.dto.response.LoginResponse;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.service.IAuthService;
import com.asdc.unicarpool.util.TokenUtil.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {

    private final IAuthService authService;

    @Autowired
    public AuthController(IAuthService authService, JwtUtil jwtUtil) {
        super(jwtUtil);
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse> authenticateUser(@RequestBody LoginRequest request) {
        LoginResponse response = (LoginResponse) authService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest,
                                                      HttpServletRequest request) {
        String bannerId = extractBannerIdFromToken(request);
        resetPasswordRequest.setBannerId(bannerId);
        BaseResponse response = authService.resetPassword(resetPasswordRequest.getBannerId(), resetPasswordRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verification-code")
    public ResponseEntity<BaseResponse> emailVerificationCode(@RequestBody VerificationCodeRequest verificationCodeRequest) {
        BaseResponse response = authService.sendEmailVerificationCode(verificationCodeRequest.getBannerId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<BaseResponse> verifyCode(@RequestBody VerificationCodeRequest verificationCodeRequest) {
        if (verificationCodeRequest.getCode() == null || verificationCodeRequest.getCode().isEmpty())
            throw new InvalidArgumentException("Verification Code is required");
        BaseResponse response = authService.verifyEmail(verificationCodeRequest.getBannerId(), Integer.parseInt(verificationCodeRequest.getCode()));
        return ResponseEntity.ok(response);

    }

    @PostMapping("/recover-password-code")
    public ResponseEntity<BaseResponse> recoverPasswordVerificationCode(@RequestBody VerificationCodeRequest verificationCodeRequest) {
        BaseResponse response = authService.sendForgetPasswordCode(verificationCodeRequest.getBannerId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recover-password")
    public ResponseEntity<BaseResponse> recoverPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        if (resetPasswordRequest.getCode() == null || resetPasswordRequest.getCode().isEmpty())
            throw new InvalidArgumentException("Verification Code is required");
        BaseResponse response = authService.recoverPassword(resetPasswordRequest.getBannerId(), Integer.parseInt(resetPasswordRequest.getCode()), resetPasswordRequest.getPassword());
        return ResponseEntity.ok(response);

    }
}
