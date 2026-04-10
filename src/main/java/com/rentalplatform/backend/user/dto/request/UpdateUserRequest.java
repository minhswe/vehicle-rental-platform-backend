package com.rentalplatform.backend.user.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String phone;
    private String avatarUrl;
}
