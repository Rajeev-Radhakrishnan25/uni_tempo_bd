package com.asdc.unicarpool.dto.request;

import com.asdc.unicarpool.model.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTypeRequest {
    @NotNull(message = "Role selection is required")
    private UserRole role;
}
