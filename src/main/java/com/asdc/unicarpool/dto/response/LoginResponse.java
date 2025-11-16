package com.asdc.unicarpool.dto.response;

import com.asdc.unicarpool.constant.AppConstant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse extends BaseResponse {
    private String token;

    @Builder.Default
    @JsonProperty("token_type")
    private String tokenType = AppConstant.Headers.BEARER_PREFIX;

    @JsonProperty("expires_in")
    private Long expiresIn;
}
