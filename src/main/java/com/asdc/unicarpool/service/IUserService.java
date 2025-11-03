package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.LoginRequest;
import com.asdc.unicarpool.dto.request.UserRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;
import com.asdc.unicarpool.model.UserRole;

public interface IUserService {

    /**
     * Register a new user
     *
     * @param userRequest UserRequest
     * @return UserResponse
     */
    public BaseResponse registerUser(UserRequest userRequest);

    /**
     * Update an existing user
     *
     * @param userRequest
     * @return UserResponse
     */
    public BaseResponse updateUser(UserRequest userRequest);

    /**
     * Add a role to a user
     *
     * @param bannerId String
     * @param role     UserRole
     * @return boolean isAdded
     */
    public boolean addUserRole(String bannerId, UserRole role);

    /**
     * Get User Details including role
     *
     * @param bannerId
     * @return
     */
    public BaseResponse getUserDetail(String bannerId);

}
