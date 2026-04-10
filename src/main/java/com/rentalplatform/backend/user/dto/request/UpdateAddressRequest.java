package com.rentalplatform.backend.user.dto.request;

import lombok.Data;

@Data
public class UpdateAddressRequest {
    private String receiverName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String detailAddress;
    private Boolean isDefault;
}
