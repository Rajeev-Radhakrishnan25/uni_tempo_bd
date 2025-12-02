package com.asdc.unicarpool.controller;

import com.asdc.unicarpool.dto.request.UserRequest;
import com.asdc.unicarpool.dto.request.UserTypeRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;
import com.asdc.unicarpool.component.TokenExtractor;
import com.asdc.unicarpool.dto.response.UserResponse;
import com.asdc.unicarpool.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final IUserService userService;
    private final TokenExtractor tokenExtractor;

    @Autowired
    public UserController(IUserService userService, TokenExtractor tokenExtractor) {
        this.userService = userService;
        this.tokenExtractor = tokenExtractor;
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse> registerUser(@Valid @RequestBody UserRequest userRequest) {
        BaseResponse response = userService.registerUser(userRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-type")
    public ResponseEntity<BaseResponse> addUserType(@Valid @RequestBody UserTypeRequest userTypeRequest,
                                                    HttpServletRequest request) {
        String bannerId = tokenExtractor.extractBannerIdFromToken(request);
        boolean isAdded = userService.addUserRole(bannerId, userTypeRequest.getRole());

        if (!isAdded) {
            return ResponseEntity.badRequest().body(new BaseResponse("User already has role " + userTypeRequest.getRole().getDisplayName()));
        } else {
            return ResponseEntity.ok(new BaseResponse("User role added successfully"));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<UserResponse> editUser(@RequestBody UserRequest userRequest,
                                                 HttpServletRequest request) {

        String bannerId = tokenExtractor.extractBannerIdFromToken(request);
        userRequest.setBannerId(bannerId);
        UserResponse response = (UserResponse) userService.updateUser(userRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUserDetails(HttpServletRequest request) {
        String bannerId = tokenExtractor.extractBannerIdFromToken(request);
        UserResponse response = (UserResponse) userService.getUserDetail(bannerId);
        return ResponseEntity.ok(response);
    }
}

