package com.asdc.unicarpool.service.impl;

import com.asdc.unicarpool.dto.request.UserRequest;
import com.asdc.unicarpool.dto.response.BaseResponse;
import com.asdc.unicarpool.dto.response.UserResponse;
import com.asdc.unicarpool.exception.ResourceNotFoundException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.UserRole;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements IUserService, UserDetailsService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Mapper mapper;

    @Autowired
    public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder, Mapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByBannerId(username).orElseThrow(() -> new UsernameNotFoundException("User not found with banner ID: " + username));

        Set<GrantedAuthority> authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User.builder().username(user.getBannerId()).password(user.getPassword()).authorities(authorities).accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!user.isEmailVerified()).build();
    }

    @Override
    public BaseResponse registerUser(UserRequest userRequest) {
        log.debug("User Request {}", userRequest);
        if (userRepository.existsByBannerId(userRequest.getBannerId()) || userRepository.existsByEmail(userRequest.getSchoolEmail())) {
            throw new ResourceNotFoundException("User already exists");
        }

        User user = User.builder().bannerId(userRequest.getBannerId()).name(userRequest.getFullName()).email(userRequest.getSchoolEmail()).password(passwordEncoder.encode(userRequest.getPassword())).emailVerified(Boolean.FALSE).phoneNumber(userRequest.getPhoneNumber()).roles(Collections.singleton(userRequest.getSelectedRole())).build();
        userRepository.save(user);
        UserResponse userResponse = new UserResponse();
        userResponse.setMessage("User Registered Successfully!");
        userResponse.setBannerId(user.getBannerId());
        return userResponse;
    }

    @Override
    public UserResponse updateUser(UserRequest userRequest) {
        log.debug("User Request {}", userRequest);
        User user = userRepository.findByBannerId(userRequest.getBannerId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(userRequest.getFullName());
        user.setEmail(userRequest.getSchoolEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        return mapper.map(userRepository.save(user), UserResponse.class);
    }

    @Override
    public boolean addUserRole(String bannerId, UserRole role) {
        log.debug("BannerId: {}, Role: {}", bannerId, role);
        User user = userRepository.findByBannerId(bannerId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRoles().contains(role)) {
            return false;
        }
        user.getRoles().add(role);
        userRepository.save(user);
        return true;
    }

    @Override
    public UserResponse getUserDetail(String bannerId) {
        log.debug("Banner Id: {}", bannerId);
        User user = userRepository.findByBannerId(bannerId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapper.map(userRepository.save(user), UserResponse.class);
    }

}
