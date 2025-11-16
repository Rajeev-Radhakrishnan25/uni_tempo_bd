package com.asdc.unicarpool.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestRequest {

    @NotNull(message = "Ride ID is required")
    @JsonProperty("ride_id")
    private Long rideId;

    @JsonProperty("message")
    private String message;
}
