package com.asdc.unicarpool.dto.request;

import com.asdc.unicarpool.model.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {

    @NotBlank(message = "Full name is required\n")
    @JsonProperty("full_name")
    private String fullName;

    @Email(message = "Invalid email format\n")
    @NotBlank(message = "Email is required\n")
    @Pattern(regexp = ".*@dal\\.ca$", message = "Must be a Dalhousie email address\n")
    @JsonProperty("school_email")
    private String schoolEmail;

    @Pattern(regexp = "^B\\d{8}$", message = "Banner ID must be in format B12345678\n")
    @NotBlank(message = "Banner ID is required\n")
    @JsonProperty("banner_id")
    private String bannerId;

    @NotBlank(message = "Phone number is required\n")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format\n")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotBlank(message = "Password is required\n")
    private String password;

    @NotNull(message = "Role selection is required\n")
    @JsonProperty("selected_role")
    private UserRole selectedRole;
}
