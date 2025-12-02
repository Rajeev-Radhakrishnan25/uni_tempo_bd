package com.asdc.unicarpool.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RideRequestStatusResponse extends BaseResponse {
    @JsonProperty("ride_id")
    private Long rideId;

    @JsonProperty("ride_request_id")
    private Long rideRequestId;

    @JsonProperty("driver_id")
    private String driverBannerId;

    @JsonProperty("driver_name")
    private String driverName;

    @JsonProperty("driver_phone_number")
    private String driverPhoneNumber;

    @JsonProperty("ride_status")
    private String rideStatus;

    @JsonProperty("ride_request_status")
    private String rideRequestStatus;

    @JsonProperty("departure_location")
    private String departureLocation;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("meeting_point")
    private String meetingPoint;

    @JsonProperty("departure_date_time")
    private LocalDateTime departureDateTime;

    @JsonProperty("reviewed")
    private Boolean reviewed;
}
