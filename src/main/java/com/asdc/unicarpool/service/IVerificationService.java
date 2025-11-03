package com.asdc.unicarpool.service;

import com.asdc.unicarpool.constant.AppConstant;
import com.asdc.unicarpool.model.User;

public interface IVerificationService {

    public Boolean sendVerificationCode(User user, AppConstant.VerificationType type);

    public Boolean verifyCode(User user, Integer code, AppConstant.VerificationType type);

}
