
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
public class LoginRequest {
    @NotBlank(message = "Missing Required field")
    @JsonProperty("banner_id")
    private String bannerId;
    @NotBlank(message = "Missing Required field")
    private String password;
}
