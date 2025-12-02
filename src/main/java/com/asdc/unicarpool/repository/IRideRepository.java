package com.asdc.unicarpool.repository;

import com.asdc.unicarpool.model.Ride;
import com.asdc.unicarpool.model.RideStatus;
import com.asdc.unicarpool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IRideRepository extends JpaRepository<Ride, Long> {
    @Query("SELECT r FROM Ride r WHERE r.driver = :driver AND r.status NOT IN ('COMPLETED', 'CANCELED') ORDER BY r.departureDateTime ASC")
    List<Ride> findUpcomingRidesByDriver(@Param("driver") User driver);

    @Query("SELECT r FROM Ride r WHERE r.departureDateTime >= :currentTime")
    List<Ride> listAllActiveRides(@Param("currentTime") LocalDateTime currentTime);
}
