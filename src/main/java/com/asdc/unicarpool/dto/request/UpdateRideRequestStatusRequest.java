package com.asdc.unicarpool.dto.request;

import com.asdc.unicarpool.model.RideRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRideRequestStatusRequest {

    @NotNull(message = "Request ID is required")
    @JsonProperty("ride_request_id")
    private Long rideRequestId;

    @JsonProperty("driver_banner_id")
    private String driverBannerId;

    @NotNull(message = "Status is required")
    @JsonProperty("status")
    private RideRequestStatus status;
}
