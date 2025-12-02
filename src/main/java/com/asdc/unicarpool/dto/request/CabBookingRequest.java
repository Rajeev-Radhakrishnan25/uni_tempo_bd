package com.asdc.unicarpool.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CabBookingRequest {

    @JsonProperty("pickup_location")
    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;

    @JsonProperty("dropoff_location")
    @NotBlank(message = "Dropoff location is required")
    private String dropoffLocation;

    @JsonProperty("passenger_count")
    @Min(value = 1, message = "Passenger count must be at least 1")
    private Integer passengerCount;
}
