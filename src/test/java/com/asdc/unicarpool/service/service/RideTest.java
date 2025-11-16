package com.asdc.unicarpool.service.service;

import com.asdc.unicarpool.dto.request.CreateRideRequest;
import com.asdc.unicarpool.dto.response.RideResponse;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.UserRole;
import com.asdc.unicarpool.repository.IRideRepository;
import com.asdc.unicarpool.repository.IUserRepository;
import com.asdc.unicarpool.service.impl.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RideTest {

    private IRideRepository rideRepository;
    private IUserRepository userRepository;
    private Mapper mapper;

    private RideService rideService;

    private User testDriver;
    private CreateRideRequest createRideRequest;
    private Ride testRide;
    private RideResponse rideResponse;
    private String driverBannerId;

    @BeforeEach
    void setUp() {

        rideRepository = mock(IRideRepository.class);
        userRepository = mock(IUserRepository.class);
        mapper = mock(Mapper.class);

        rideService = new RideService(userRepository, rideRepository, mapper);


        testDriver = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@dal.ca")
                .bannerId("B00123456")
                .phoneNumber("1234567890")
                .roles(Set.of(UserRole.DRIVER))
                .build();

        createRideRequest = CreateRideRequest.builder()
                .departureLocation("Downtown Halifax")
                .destination("Darthmouth")
                .departureDateTime(LocalDateTime.of(2026, 11, 15, 9, 0))
                .availableSeats(3)
                .meetingPoint("Goldberg CS Building")
                .rideConditions("No smoking, music allowed")
                .build();

        testRide = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Downtown Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2026, 11, 15, 9, 0))
                .availableSeats(3)
                .meetingPoint("Goldberg CS Building")
                .rideConditions("No smoking, music allowed")
                .build();

        rideResponse = RideResponse.builder()
                .id(1L)
                .driverName("Sanif Ali")
                .driverId(driverBannerId)
                .departureLocation("Downtown Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2026, 11, 15, 9, 0))
                .availableSeats(3)
                .meetingPoint("Goldberg CS Building")
                .rideConditions("No smoking, music allowed")
                .createdAt(LocalDateTime.now())
                .build();
        driverBannerId = "B00979961";
    }

    @Test
    void createRide_Success() {
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);
        when(mapper.map(any(Ride.class), eq(RideResponse.class))).thenReturn(rideResponse);

        RideResponse result = rideService.createNewRide(createRideRequest, driverBannerId);

        assertNotNull(result);
        assertEquals("Downtown Halifax", result.getDepartureLocation());
        assertEquals("Dartmouth", result.getDestination());
        assertEquals(3, result.getAvailableSeats());
        assertEquals("John Doe", result.getDriverName());


        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository).save(any(Ride.class));
        verify(mapper).map(any(Ride.class), eq(RideResponse.class));
    }

    @Test
    void createRide_UserNotFound_ThrowsException() {
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            rideService.createNewRide(createRideRequest, driverBannerId);
        });

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void createRide_NotADriver_ThrowsException() {
        User testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@dal.ca")
                .bannerId("B00123456")
                .phoneNumber("1234567890")
                .roles(Set.of(UserRole.RIDER))
                .build();
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidCredentialsException.class, () -> {
            rideService.createNewRide(createRideRequest, driverBannerId);
        });

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void createRide_OlderDepartureTime_ThrowsException() {

        CreateRideRequest pastRideRequest = CreateRideRequest.builder()
                .departureLocation("Downtown Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2023, 11, 15, 9, 0)) // Past date
                .availableSeats(3)
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));

        assertThrows(InvalidArgumentException.class, () -> {
            rideService.createNewRide(pastRideRequest, driverBannerId);
        });

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void getActiveRidesByDriver_Success() {
        Ride activeRide1 = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2025, 11, 20, 10, 0)) // Future date
                .availableSeats(3)
                .build();

        Ride activeRide2 = Ride.builder()
                .id(2L)
                .driver(testDriver)
                .departureLocation("Dartmouth")
                .destination("Truro")
                .departureDateTime(LocalDateTime.of(2025, 11, 25, 14, 30)) // Future date
                .availableSeats(2)
                .build();

        List<Ride> activeRides = Arrays.asList(activeRide1, activeRide2);

        RideResponse response1 = RideResponse.builder()
                .id(1L)
                .driverName("John Doe")
                .driverId(driverBannerId)
                .departureLocation("Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2025, 11, 20, 10, 0))
                .availableSeats(3)
                .createdAt(LocalDateTime.now())
                .build();

        RideResponse response2 = RideResponse.builder()
                .id(2L)
                .driverName("John Doe")
                .driverId(driverBannerId)
                .departureLocation("Dartmouth")
                .destination("Truro")
                .departureDateTime(LocalDateTime.of(2025, 11, 25, 14, 30))
                .availableSeats(2)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findUpcomingRidesByDriver(eq(testDriver), any(LocalDateTime.class)))
                .thenReturn(activeRides);
        when(mapper.map(activeRide1, RideResponse.class)).thenReturn(response1);
        when(mapper.map(activeRide2, RideResponse.class)).thenReturn(response2);

        List<RideResponse> result = rideService.getActiveRidesByDriver(driverBannerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Halifax", result.get(0).getDepartureLocation());
        assertEquals("Dartmouth", result.get(1).getDepartureLocation());
        assertEquals("John Doe", result.get(0).getDriverName());
        assertEquals("John Doe", result.get(1).getDriverName());

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository).findUpcomingRidesByDriver(eq(testDriver), any(LocalDateTime.class));
        verify(mapper).map(activeRide1, RideResponse.class);
        verify(mapper).map(activeRide2, RideResponse.class);
    }

    @Test
    void getActiveRidesByDriver_UserNotFound_ThrowsException() {
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            rideService.getActiveRidesByDriver(driverBannerId);
        });
        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository, never()).findUpcomingRidesByDriver(any(User.class), any(LocalDateTime.class));
    }

    @Test
    void getActiveRidesByDriver_NoActiveRides_ReturnsEmptyList() {
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findUpcomingRidesByDriver(eq(testDriver), any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<RideResponse> result = rideService.getActiveRidesByDriver(driverBannerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository).findUpcomingRidesByDriver(eq(testDriver), any(LocalDateTime.class));
    }


    @Test
    void getAllActiveRides_Success() {
        // Arrange
        Ride activeRide1 = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2025, 11, 20, 10, 0)) // Future date
                .availableSeats(3)
                .build();

        Ride activeRide2 = Ride.builder()
                .id(2L)
                .driver(testDriver)
                .departureLocation("Dartmouth")
                .destination("Truro")
                .departureDateTime(LocalDateTime.of(2025, 11, 25, 14, 30)) // Future date
                .availableSeats(2)
                .build();

        RideResponse response1 = RideResponse.builder()
                .id(1L)
                .driverName("John Doe")
                .driverId(driverBannerId)
                .departureLocation("Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2025, 11, 20, 10, 0))
                .availableSeats(3)
                .createdAt(LocalDateTime.now())
                .build();

        RideResponse response2 = RideResponse.builder()
                .id(2L)
                .driverName("John Doe")
                .driverId(driverBannerId)
                .departureLocation("Dartmouth")
                .destination("Truro")
                .departureDateTime(LocalDateTime.of(2025, 11, 25, 14, 30))
                .availableSeats(2)
                .createdAt(LocalDateTime.now())
                .build();

        when(rideRepository.listAllActiveRides(any(LocalDateTime.class)))
                .thenReturn(List.of(activeRide1, activeRide2));
        when(mapper.map(activeRide1, RideResponse.class)).thenReturn(response1);
        when(mapper.map(activeRide2, RideResponse.class)).thenReturn(response2);

        // Act
        List<RideResponse> result = rideService.getAllActiveRides(driverBannerId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Halifax", result.get(0).getDepartureLocation());
        assertEquals("Truro", result.get(1).getDestination());
        assertEquals("John Doe", result.get(0).getDriverName());
        assertEquals("John Doe", result.get(1).getDriverName());

        verify(rideRepository).listAllActiveRides(any(LocalDateTime.class));
        verify(mapper).map(activeRide1, RideResponse.class);
        verify(mapper).map(activeRide2, RideResponse.class);
    }

    @Test
    void getAllActiveRides_NoActiveRides_ReturnsEmptyList() {
        when(rideRepository.listAllActiveRides(any(LocalDateTime.class)))
                .thenReturn(List.of());
        List<RideResponse> result = rideService.getAllActiveRides(driverBannerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rideRepository).listAllActiveRides(any(LocalDateTime.class));
        verify(mapper, never()).map(any(Ride.class), eq(RideResponse.class));
    }


}
