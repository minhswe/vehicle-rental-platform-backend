package com.rentalplatform.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAddressRequest {
    @NotBlank
    private String receiverName;

    @NotBlank
    private String phone;

    @NotBlank
    private String province;

    @NotBlank
    private String district;

    @NotBlank
    private String ward;

    @NotBlank
    private String detailAddress;

    @NotBlank
    private Boolean isDefault;
}
