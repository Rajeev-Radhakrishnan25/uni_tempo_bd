package com.asdc.unicarpool.dto.response;

import com.asdc.unicarpool.model.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse extends BaseResponse {
    private int id;
    private String name;
    private String email;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("banner_id")
    private String bannerId;
    private Set<UserRole> roles;
    @JsonProperty("email_verified")
    private Boolean emailVerified;

}
