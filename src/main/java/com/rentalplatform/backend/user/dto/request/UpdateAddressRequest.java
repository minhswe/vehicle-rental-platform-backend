package com.rentalplatform.backend.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAddressRequest {
    private String receiverName;

    private String phone;

    private String province;

    private String district;

    private String ward;

    private String detailAddress;

    private boolean isDefault;
}
