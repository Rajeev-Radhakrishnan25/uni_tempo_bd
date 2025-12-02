package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.CabBookingRequest;
import com.asdc.unicarpool.dto.response.CabBookingResponse;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.CabBooking;
import com.asdc.unicarpool.model.CabBookingStatus;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.UserRole;
import com.asdc.unicarpool.repository.ICabBookingRepository;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.impl.CabBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Driver;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CabBookingServiceTest {

    private ICabBookingRepository cabBookingRepository;
    private IUserRepository userRepository;
    private Mapper mapper;
    private CabBookingService cabBookingService;

    private User testRider;
    private User testDriver;
    private String riderBannerId;

    @BeforeEach
    void setUp() {
        cabBookingRepository = mock(ICabBookingRepository.class);
        userRepository = mock(IUserRepository.class);
        mapper = mock(Mapper.class);

        cabBookingService = new CabBookingService(cabBookingRepository, userRepository, mapper);

        riderBannerId = "B12345678";

        testRider = User.builder()
                .id(1L)
                .name("Jane Smith")
                .email("jane@dal.ca")
                .bannerId(riderBannerId)
                .phoneNumber("0987654321")
                .roles(Set.of(UserRole.RIDER))
                .build();


        testDriver = User.builder()
                .id(2L)
                .name("Mostafaa Abdelaziz")
                .email("ms582520@dal.ca")
                .bannerId("B00875982")
                .phoneNumber("9022372694")
                .roles(Set.of(UserRole.DRIVER))
                .isAvailable(true)
                .build();
    }

    @Test
    void createCabBooking_Success() {

        CabBookingRequest request = CabBookingRequest.builder()
                .pickupLocation("Halifax Downtown")
                .dropoffLocation("Dalhousie University")
                .passengerCount(2)
                .build();

        CabBooking savedBooking = CabBooking.builder()
                .id(1L)
                .driver(testDriver)
                .rider(testRider)
                .pickupLocation("Halifax Downtown")
                .dropoffLocation("Dalhousie University")
                .passengerCount(2)
                .status(CabBookingStatus.PENDING)
                .estimatedFare(25.0)
                .arrivalTime(LocalDateTime.of(2026, 11, 20, 10, 30))
                .build();

        CabBookingResponse expectedResponse = CabBookingResponse.builder()
                .bookingId(1L)
                .pickupLocation("Halifax Downtown")
                .dropoffLocation("Dalhousie University")
                .passengerCount(2)
                .status(CabBookingStatus.PENDING.name())
                .estimatedFare(25.0)
                .arrivalTime(savedBooking.getArrivalTime())
                .build();

        when(userRepository.findByBannerId(riderBannerId)).thenReturn(Optional.of(testRider));
        when(userRepository.findByRolesContainingAndIsAvailableTrue(UserRole.DRIVER))
                .thenReturn(List.of(testDriver));
        when(cabBookingRepository.save(any(CabBooking.class))).thenReturn(savedBooking);
        when(mapper.map(any(CabBooking.class), eq(CabBookingResponse.class)))
                .thenReturn(expectedResponse);
        CabBookingResponse result = cabBookingService.createCabBooking(request, riderBannerId);


        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
        assertEquals("Halifax Downtown", result.getPickupLocation());
        assertEquals("Dalhousie University", result.getDropoffLocation());
        assertEquals(2, result.getPassengerCount());
        assertEquals(CabBookingStatus.PENDING.name(), result.getStatus());

        verify(userRepository).findByBannerId(riderBannerId);
        verify(cabBookingRepository).save(any(CabBooking.class));
        verify(mapper).map(any(CabBooking.class), eq(CabBookingResponse.class));
    }

    @Test
    void createCabBooking_UserNotFound_Exception() {
        CabBookingRequest request = CabBookingRequest.builder()
                .pickupLocation("A")
                .dropoffLocation("B")
                .passengerCount(1)
                .build();

        when(userRepository.findByBannerId(riderBannerId)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> cabBookingService.createCabBooking(request, riderBannerId));

        verify(userRepository,never()).findByRolesContainingAndIsAvailableTrue(UserRole.DRIVER);
        verify(cabBookingRepository,never()).save(any(CabBooking.class));
        verify(mapper,never()).map(any(CabBooking.class), eq(CabBookingResponse.class));
    }


    @Test
    void createCabBooking_DriverNotAvailable_Exception() {
        CabBookingRequest request = CabBookingRequest.builder()
                .pickupLocation("A")
                .dropoffLocation("B")
                .passengerCount(1)
                .build();

        when(userRepository.findByBannerId(riderBannerId)).thenReturn(Optional.of(testRider));
        when(userRepository.findByRolesContainingAndIsAvailableTrue(UserRole.DRIVER))
                .thenReturn(List.of());
        CabBookingResponse result = cabBookingService.createCabBooking(request, riderBannerId);

        assertEquals("NO_DRIVERS_AVAILABLE",result.getStatus());
        verify(cabBookingRepository,never()).save(any(CabBooking.class));
        verify(mapper,never()).map(any(CabBooking.class), eq(CabBookingResponse.class));
    }

}
