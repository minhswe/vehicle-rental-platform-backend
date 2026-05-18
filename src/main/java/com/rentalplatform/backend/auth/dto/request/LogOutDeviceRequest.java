package com.rentalplatform.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogOutDeviceRequest {

    @NotBlank
    private String refreshToken;
}
