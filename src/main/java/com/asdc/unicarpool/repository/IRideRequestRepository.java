package com.asdc.unicarpool.repository;

import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.RideRequest;
import com.asdc.unicarpool.model.RideRequestStatus;
import com.asdc.unicarpool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IRideRequestRepository extends JpaRepository<RideRequest, Long> {

    boolean existsByRideAndRider(Ride ride, User rider);

    List<RideRequest> findByRideDriverAndStatus(User driver, RideRequestStatus status);

    Optional<RideRequest> findByRideDriverAndId(User driver, Long id);

    List<RideRequest> findByRiderAndStatusIn(User rider, List<RideRequestStatus> statuses);

    @Query("SELECT rr FROM RideRequest rr WHERE rr.rider = :rider AND rr.status = 'ACCEPTED' AND rr.ride.status IN ('WAITING', 'STARTED') ORDER BY rr.ride.departureDateTime ASC")
    List<RideRequest> findCurrentBookingsForRider(User rider);
}
