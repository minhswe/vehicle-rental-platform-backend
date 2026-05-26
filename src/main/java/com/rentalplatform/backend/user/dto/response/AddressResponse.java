package com.rentalplatform.backend.user.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddressResponse {
    private UUID id;

    private String receiverName;

    private String phone;

    private String province;

    private String district;

    private String ward;

    private String detailAddress;

    private boolean isDefault;
}
