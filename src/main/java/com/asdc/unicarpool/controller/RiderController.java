package com.asdc.unicarpool.controller;

import com.asdc.unicarpool.dto.request.UserRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;
import com.asdc.unicarpool.service.IRideService;
import com.asdc.unicarpool.service.impl.RideService;
import com.asdc.unicarpool.util.TokenUtil.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rider")
public class RiderController extends BaseController {

    private final IRideService rideService;

    @Autowired
    public RiderController(RideService rideService, JwtUtil jwtUtil) {
        super(jwtUtil);
        this.rideService = rideService;
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponse> bookARide(@Valid @RequestBody UserRequest userRequest) {

        return ResponseEntity.ok(new BaseResponse("Success"));
    }


}
