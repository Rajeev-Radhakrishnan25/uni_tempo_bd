package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.LoginRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;

public interface IAuthService {
    /**
     * Authenticate a user
     *
     * @param loginRequest LoginRequest
     * @return LoginResponse
     */
    public BaseResponse authenticateUser(LoginRequest loginRequest);

    /**
     * Reset Password for already logged in user
     *
     * @param bannerId
     * @param password
     * @return
     */
    public BaseResponse resetPassword(String bannerId, String password);

    /**
     * Verification code for new user
     *
     * @param bannerId
     * @return
     */
    public BaseResponse sendEmailVerificationCode(String bannerId);

    /**
     *  verify email using code
     *
     * @param bannerId
     * @param code
     * @return
     */
    public BaseResponse verifyEmail(String bannerId, Integer code);

    /**
     * send code to user email for forget password
     *
     * @param bannerId
     * @return
     */
    public BaseResponse sendForgetPasswordCode(String bannerId);

    /**
     * Set New Password
     *
     * @param bannerId
     * @param code
     * @param password
     * @return
     */
    public BaseResponse recoverPassword(String bannerId, Integer code, String password);

}
