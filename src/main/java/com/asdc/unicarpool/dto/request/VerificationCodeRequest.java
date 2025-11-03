package com.asdc.unicarpool.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerificationCodeRequest {
    @NotBlank(message = "Banner ID is required\n")
    @JsonProperty("banner_id")
    private String bannerId;
    @JsonProperty("verification_code")
    private String code;
}
