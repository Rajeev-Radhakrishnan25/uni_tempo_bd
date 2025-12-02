package com.asdc.unicarpool.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cab_booking")
public class CabBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @Column(name = "pickup_location", nullable = false)
    private String pickupLocation;

    @Column(name = "dropoff_location", nullable = false)
    private String dropoffLocation;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CabBookingStatus status = CabBookingStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "estimated_fare")
    private Double estimatedFare;

    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;
}
