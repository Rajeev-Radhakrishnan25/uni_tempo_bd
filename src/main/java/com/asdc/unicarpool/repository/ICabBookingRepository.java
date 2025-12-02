package com.asdc.unicarpool.repository;

import com.asdc.unicarpool.model.CabBooking;
import com.asdc.unicarpool.model.CabBookingStatus;
import com.asdc.unicarpool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICabBookingRepository extends JpaRepository<CabBooking, Long> {
    List<CabBooking> findByRiderAndStatusIn(User rider, List<CabBookingStatus> pending);
}
