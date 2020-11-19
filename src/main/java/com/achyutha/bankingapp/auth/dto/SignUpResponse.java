package com.achyutha.bankingapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * JwtResponse DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SignUpResponse {

    private Long Id;

    private String userName;

    private String tempPassword;
}
