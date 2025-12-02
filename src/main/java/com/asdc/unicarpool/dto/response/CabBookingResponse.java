package com.asdc.unicarpool.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CabBookingResponse extends BaseResponse {

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("driver_id")
    private Long driverId;

    @JsonProperty("driver_name")
    private String driverName;

    @JsonProperty("vehicle_details")
    private String vehicleDetails;

    @JsonProperty("eta_minutes")
    private Integer etaMinutes;

    @JsonProperty("pickup_location")
    private String pickupLocation;

    @JsonProperty("dropoff_location")
    private String dropoffLocation;

    @JsonProperty("passenger_count")
    private Integer passengerCount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("estimated_fare")
    private Double estimatedFare;

    @JsonProperty("arrival_time")
    private LocalDateTime arrivalTime;

}
