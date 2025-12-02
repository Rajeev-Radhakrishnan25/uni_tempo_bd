package com.asdc.unicarpool.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;



@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "ride_id", "passenger_id"
                        }
                )
        }
)
@Getter
@Setter
public class RatingReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(nullable = false)
    private int rating;

    private String comment;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

}
