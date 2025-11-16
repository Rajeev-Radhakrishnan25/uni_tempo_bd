package com.asdc.unicarpool.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRideRequest {
    @NotBlank(message = "Departure location is required")
    @JsonProperty("departure_location")
    private String departureLocation;

    @NotBlank(message = "Destination is required")
    @JsonProperty("destination")
    private String destination;

    @NotNull(message = "Departure date and time is required")
    @Future(message = "Departure date and time must be in the future")
    @JsonProperty("departure_date_time")
    private LocalDateTime departureDateTime;

    @NotNull(message = "Available seats is required")
    @JsonProperty("available_seats")
    private Integer availableSeats;

    @NotBlank(message = "Meeting Point is required")
    @JsonProperty("meeting_point")
    private String meetingPoint;

    @NotBlank(message = "Ride Condition is required")
    @JsonProperty("ride_conditions")
    private String rideConditions;
}
