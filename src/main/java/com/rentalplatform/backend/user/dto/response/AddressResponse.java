package com.rentalplatform.backend.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AddressResponse {
    private UUID id;
    private String receiverName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String detailAddress;
    private Boolean isDefault;
}
