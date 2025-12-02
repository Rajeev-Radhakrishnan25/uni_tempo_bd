package com.asdc.unicarpool.service;

import com.asdc.unicarpool.dto.request.CreateRideRequest;
import com.asdc.unicarpool.dto.response.RideResponse;
import com.asdc.unicarpool.exception.InvalidArgumentException;
import com.asdc.unicarpool.exception.InvalidCredentialsException;
import com.asdc.unicarpool.exception.ValidationException;
import com.asdc.unicarpool.mapper.Mapper;
import com.asdc.unicarpool.model.*;
import com.asdc.unicarpool.repository.*;
import com.asdc.unicarpool.service.impl.RideService;
import com.asdc.unicarpool.util.EmailUtil.IEmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.asdc.unicarpool.constant.AppConstant.EMAIL_TEMPLATE_RIDE_STATUS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RideTest {

    private IRideRepository rideRepository;
    private IUserRepository userRepository;
    private IRatingReviewRepository ratingReviewRepository;
    private IRideRequestRepository rideRequestRepository;
    private IEmailUtil emailUtil;
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
        rideRequestRepository = mock(IRideRequestRepository.class);
        emailUtil = mock(IEmailUtil.class);
        ratingReviewRepository = mock(IRatingReviewRepository.class);


        mapper = mock(Mapper.class);
        rideService = new RideService(userRepository, rideRepository, rideRequestRepository, emailUtil,ratingReviewRepository ,mapper);

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
                .status("Waiting")
                .build();

        RideResponse response2 = RideResponse.builder()
                .id(2L)
                .driverName("John Doe")
                .driverId(driverBannerId)
                .departureLocation("Dartmouth")
                .destination("Truro")
                .departureDateTime(LocalDateTime.of(2025, 11, 25, 14, 30))
                .availableSeats(2)
                .status("Waiting")
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findUpcomingRidesByDriver(eq(testDriver)))
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
        verify(rideRepository).findUpcomingRidesByDriver(eq(testDriver));
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
        verify(rideRepository, never()).findUpcomingRidesByDriver(any(User.class));
    }

    @Test
    void getActiveRidesByDriver_NoActiveRides_ReturnsEmptyList() {
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findUpcomingRidesByDriver(eq(testDriver)))
                .thenReturn(List.of());

        List<RideResponse> result = rideService.getActiveRidesByDriver(driverBannerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository).findUpcomingRidesByDriver(eq(testDriver));
    }


    @Test
    void getAllActiveRides_Success() {
        // Arrange
        Ride activeRide1 = Ride.builder()
                .id(1L)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2026, 11, 20, 10, 0)) // Future date
                .availableSeats(3)
                .build();

        Ride activeRide2 = Ride.builder()
                .id(2L)
                .driver(testDriver)
                .departureLocation("Dartmouth")
                .destination("Truro")
                .departureDateTime(LocalDateTime.of(2026, 11, 25, 14, 30)) // Future date
                .availableSeats(2)
                .build();

        RideResponse response1 = RideResponse.builder()
                .id(1L)
                .driverName("John Doe")
                .driverId(driverBannerId)
                .departureLocation("Halifax")
                .destination("Dartmouth")
                .departureDateTime(LocalDateTime.of(2026, 11, 20, 10, 0))
                .availableSeats(3)
                .status("Waiting")
                .build();

        RideResponse response2 = RideResponse.builder()
                .id(2L)
                .driverName("John Doe")
                .driverId(driverBannerId)
                .departureLocation("Dartmouth")
                .destination("Truro")
                .departureDateTime(LocalDateTime.of(2026, 11, 25, 14, 30))
                .availableSeats(2)
                .status("Waiting")
                .build();

        when(rideRepository.listAllActiveRides(any(LocalDateTime.class)))
                .thenReturn(List.of(activeRide1, activeRide2));
        when(mapper.map(activeRide1, RideResponse.class)).thenReturn(response1);
        when(mapper.map(activeRide2, RideResponse.class)).thenReturn(response2);
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));

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

    @Test
    void updateRideStatus_DriverNotFound_ThrowsException() {
        Long rideId = 1L;
        RideStatus newStatus = RideStatus.STARTED;

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            rideService.updateRideStatus(rideId, newStatus, driverBannerId);
        });

        verify(userRepository).findByBannerId(driverBannerId);
        verify(rideRepository, never()).findById(any());
        verify(rideRepository, never()).save(any());
    }

    @Test
    void updateRideStatus_RideNotFound_ThrowsException() {
        Long rideId = 999L;
        RideStatus newStatus = RideStatus.STARTED;

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> {
            rideService.updateRideStatus(rideId, newStatus, driverBannerId);
        });

        verify(rideRepository).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    void updateRideStatus_DriverMismatch_ThrowsException() {
        Long rideId = 1L;
        RideStatus newStatus = RideStatus.STARTED;

        User anotherDriver = User.builder()
                .id(2L)
                .name("Jane Smith")
                .bannerId("B00987654")
                .roles(Set.of(UserRole.DRIVER))
                .build();

        Ride ride = Ride.builder()
                .id(rideId)
                .driver(anotherDriver)
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThrows(ValidationException.class, () -> {
            rideService.updateRideStatus(rideId, newStatus, driverBannerId);
        });

        verify(rideRepository).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    void updateRideStatus_Success() {
        Long rideId = 1L;
        RideStatus newStatus = RideStatus.STARTED;

        Ride ride = Ride.builder()
                .id(rideId)
                .driver(testDriver)
                .status(RideStatus.WAITING)
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        boolean result = rideService.updateRideStatus(rideId, newStatus, driverBannerId);

        assertTrue(result);
        assertEquals(newStatus, ride.getStatus());

        verify(rideRepository).findById(rideId);
        verify(rideRepository).save(ride);
    }

    @Test
    void updateRideStatus_AlreadyCompleted_ThrowsException() {
        Long rideId = 1L;
        RideStatus newStatus = RideStatus.STARTED;

        Ride ride = Ride.builder()
                .id(rideId)
                .driver(testDriver)
                .status(RideStatus.COMPLETED)
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThrows(ValidationException.class, () -> {
            rideService.updateRideStatus(rideId, newStatus, driverBannerId);
        });

        verify(rideRepository).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    void updateRideStatus_AlreadyCancelled_ThrowsException() {
        Long rideId = 1L;
        RideStatus newStatus = RideStatus.STARTED;

        Ride ride = Ride.builder()
                .id(rideId)
                .driver(testDriver)
                .status(RideStatus.CANCELLED)
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThrows(ValidationException.class, () -> {
            rideService.updateRideStatus(rideId, newStatus, driverBannerId);
        });

        verify(rideRepository).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    void updateRideStatus_NoStartedStatusChange_ThrowsException() {
        Long rideId = 1L;
        RideStatus newStatus = RideStatus.COMPLETED;

        Ride ride = Ride.builder()
                .id(rideId)
                .driver(testDriver)
                .status(RideStatus.WAITING)
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThrows(ValidationException.class, () -> {
            rideService.updateRideStatus(rideId, newStatus, driverBannerId);
        });

        verify(rideRepository).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @Test
    void getUserByBannerId_success(){
        User user = new User();
        user.setId(1L);
        user.setBannerId("B00875982");

        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.of(user));

        User result = rideService.getUserByBannerId("B00875982");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("B00875982", result.getBannerId());

        verify(userRepository).findByBannerId("B00875982");

    }

    @Test
    void getUserByBannerId_fail(){
        when(userRepository.findByBannerId("B00875982")).thenReturn(Optional.empty());

        User result =  rideService.getUserByBannerId("B00875982");
        assertNull(result);
        verify(userRepository).findByBannerId("B00875982");
    }

    @Test
    void updateRideStatus_SendsEmail_Success() {
        ReflectionTestUtils.setField(rideService, "appName", "TestApp");

        Long rideId = 1L;
        RideStatus newStatus = RideStatus.STARTED;
        User rider1 = User.builder()
                .id(2L)
                .name("Alice Rider")
                .email("alice@dal.ca")
                .bannerId("B00111111")
                .build();
        User rider2 = User.builder()
                .id(3L)
                .name("Bob Rider")
                .email("bob@dal.ca")
                .bannerId("B00222222")
                .build();
        Ride ride = Ride.builder()
                .id(rideId)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.of(2025, 11, 25, 10, 0))
                .availableSeats(3)
                .status(RideStatus.WAITING)
                .build();

        RideRequest request1 = RideRequest.builder()
                .id(1L)
                .ride(ride)
                .rider(rider1)
                .status(RideRequestStatus.ACCEPTED)
                .build();

        RideRequest request2 = RideRequest.builder()
                .id(2L)
                .ride(ride)
                .rider(rider2)
                .status(RideRequestStatus.ACCEPTED)
                .build();

        List<RideRequest> acceptedRequests = Arrays.asList(request1, request2);
        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRequestRepository.findByRideAndStatus(ride, RideRequestStatus.ACCEPTED))
                .thenReturn(acceptedRequests);
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        rideService.updateRideStatus(rideId, newStatus, driverBannerId);

        verify(rideRequestRepository).findByRideAndStatus(ride, RideRequestStatus.ACCEPTED);
        verify(emailUtil).sendEmail(
                eq("alice@dal.ca"),
                eq("Ride Status Update - TestApp"),
                eq(EMAIL_TEMPLATE_RIDE_STATUS),
                anyMap()
        );

        verify(emailUtil).sendEmail(
                eq("bob@dal.ca"),
                eq("Ride Status Update - TestApp"),
                eq(EMAIL_TEMPLATE_RIDE_STATUS),
                anyMap()
        );
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void updateRideStatus_NoAcceptedRiders_NoEmailsSent() {
        Long rideId = 1L;
        RideStatus newStatus = RideStatus.STARTED;

        Ride ride = Ride.builder()
                .id(rideId)
                .driver(testDriver)
                .departureLocation("Halifax")
                .destination("Sydney")
                .departureDateTime(LocalDateTime.now().plusHours(1))
                .availableSeats(3)
                .status(RideStatus.WAITING)
                .build();

        when(userRepository.findByBannerId(driverBannerId)).thenReturn(Optional.of(testDriver));
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRequestRepository.findByRideAndStatus(ride, RideRequestStatus.ACCEPTED))
                .thenReturn(List.of());
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        rideService.updateRideStatus(rideId, newStatus, driverBannerId);

        verify(rideRequestRepository).findByRideAndStatus(ride, RideRequestStatus.ACCEPTED);
        verify(emailUtil, never()).sendEmail(anyString(), anyString(), anyString(), anyMap());
        verify(rideRepository).save(any(Ride.class));
    }
}
