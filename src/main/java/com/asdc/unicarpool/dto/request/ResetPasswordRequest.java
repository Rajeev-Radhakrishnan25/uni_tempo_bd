package com.asdc.unicarpool.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {
    @JsonProperty("banner_id")
    private String bannerId;
    @JsonProperty("verification_code")
    private String code;
    @NotBlank(message = "Password is required\n")
    private String password;
}
