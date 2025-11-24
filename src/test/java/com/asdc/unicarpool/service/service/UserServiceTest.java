package com.asdc.unicarpool.service.service;

import com.asdc.unicarpool.dto.request.UserRequest;
import com.asdc.unicarpool.dto.response.UserResponse;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.UserRole;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserRequest request;
    private User testUser;

    private IUserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private Mapper mapper;

   @BeforeEach
    void setUp() {

       userRepository = mock(IUserRepository.class);
       passwordEncoder = mock(PasswordEncoder.class);
       mapper = mock(Mapper.class);


       userService = new UserService(userRepository, passwordEncoder, mapper);

       testUser = User.builder()
               .id(1L)
               .name("Mostafaa Abdelaziz")
               .email("mostafaa@dal.ca")
               .bannerId("B00875982")
               .phoneNumber("+19022372694")
               .password("Pass@123")
               .roles(new HashSet<>(Set.of(UserRole.RIDER)))
               .build();

       request = new UserRequest();
       request.setFullName("Mostafaa Abdelaziz");
       request.setSchoolEmail("mostafaa@dal.ca");
       request.setBannerId("B00875982");
       request.setPhoneNumber("+19022372694");
       request.setPassword("Pass@123");
       request.setSelectedRole(UserRole.RIDER);
    }

@Test
    void loadUserByUsername_Success() {
        // Arrange: mock repository to return testUser by bannerId
        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername("B00875982");

        assertNotNull(userDetails);
        assertEquals(testUser.getBannerId(), userDetails.getUsername());
        assertEquals(testUser.getPassword(), userDetails.getPassword());
        assertTrue(testUser.getRoles().contains(UserRole.RIDER));


        verify(userRepository).findByBannerId("B00875982");
    }

    @Test
    void loadUserByUsername_NotFound(){
        // Arranging mockito to return empty optional
        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.loadUserByUsername("B00875982"));

        verify(userRepository).findByBannerId("B00875982");
    }

    @Test
    void registerUser_Success(){
       when(userRepository.existsByBannerId(request.getBannerId())).thenReturn(false);
       when(userRepository.existsByEmail(request.getSchoolEmail())).thenReturn(false);
       when(passwordEncoder.encode(request.getPassword())).thenReturn("Pass@123");

        UserResponse response = (UserResponse) userService.registerUser(request);

        assertNotNull(response);
        assertEquals(request.getBannerId(),response.getBannerId());
        assertEquals("User Registered Successfully!", response.getMessage());

        verify(userRepository).existsByBannerId(request.getBannerId());
    }

    @Test
    void registerUser_UserAlreadyExists_ThrowsException(){
        when(userRepository.existsByBannerId(request.getBannerId())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(request));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException(){
        when(userRepository.existsByBannerId(request.getBannerId())).thenReturn(false);
        when(userRepository.existsByEmail(request.getSchoolEmail())).thenReturn(true);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("Pass@123");

        assertThrows(RuntimeException.class, () -> userService.registerUser(request));
    }

    @Test
    void updateUser_Success(){
        when(userRepository.findByBannerId(request.getBannerId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(mapper.map(testUser, UserResponse.class)).thenReturn(new UserResponse());

        UserResponse response = userService.updateUser(request);
        assertNotNull(response);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_UserNotFound_ThrowsException(){
        when(userRepository.findByBannerId(request.getBannerId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser(request));
    }

    @Test
    void addUserRoles_Success(){
        when(userRepository.findByBannerId(request.getBannerId())).thenReturn(Optional.of(testUser));
        boolean add = userService.addUserRole(testUser.getBannerId(), UserRole.DRIVER);

        assertTrue(add);
        assertTrue(testUser.getRoles().contains(UserRole.DRIVER));
        verify(userRepository).save(testUser);
    }

    @Test
    void addUserRoles_UserNotFound_ThrowsException(){
        when(userRepository.findByBannerId(request.getBannerId())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.addUserRole(request.getBannerId(), UserRole.DRIVER));
    }

    @Test
    void addUserRoles_RoleAlreadyExists(){
       when(userRepository.findByBannerId(request.getBannerId())).thenReturn(Optional.of(testUser));
        boolean add = userService.addUserRole(testUser.getBannerId(), UserRole.RIDER);

        assertFalse(add);
        verify(userRepository, never()).save(testUser);
   }

   @Test
    void addUserRole_UserNotFound_ThrowsException(){
       when(userRepository.findByBannerId(request.getBannerId())).thenReturn(Optional.empty());
       assertThrows(RuntimeException.class, () -> userService.addUserRole(request.getBannerId(), UserRole.RIDER));
    }

    @Test
    void getUserDetail_Success(){
        when(userRepository.findByBannerId(request.getBannerId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(mapper.map(testUser, UserResponse.class)).thenReturn(new UserResponse());

        UserResponse response = userService.getUserDetail(request.getBannerId());
        assertNotNull(response);
        verify(userRepository).save(testUser);
    }

    @Test
    void getUserDetail_UserNotFound_ThrowsException(){
       when(userRepository.findByBannerId(request.getBannerId())).thenReturn(Optional.empty());
       assertThrows(RuntimeException.class, () -> userService.getUserDetail(request.getBannerId()));
    }
}