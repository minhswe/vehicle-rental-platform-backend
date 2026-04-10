package com.rentalplatform.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDriverLisenseRequest {

    @NotBlank
    private String licenseNumber;

    @NotBlank
    private String frontImageUrl;

    @NotBlank
    private String backImageUrl;

    @NotBlank
    private LocalDate expiryDate;
}
