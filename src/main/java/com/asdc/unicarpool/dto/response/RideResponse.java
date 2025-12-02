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
public class RideResponse extends BaseResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("driver_name")
    private String driverName;

    @JsonProperty("driver_id")
    private String driverId;

    @JsonProperty("departure_location")
    private String departureLocation;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("departure_date_time")
    private LocalDateTime departureDateTime;

    @JsonProperty("available_seats")
    private Integer availableSeats;

    @JsonProperty("meeting_point")
    private String meetingPoint;

    @JsonProperty("ride_conditions")
    private String rideConditions;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type")
    private String type;

    @JsonProperty("reviewed")
    private Boolean reviewed;
}
