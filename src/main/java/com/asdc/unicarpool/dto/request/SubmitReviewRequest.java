package com.asdc.unicarpool.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitReviewRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Rating is required")
    @Min(1)
    @Max(5)
    private int rating;
    private String comment;
}
