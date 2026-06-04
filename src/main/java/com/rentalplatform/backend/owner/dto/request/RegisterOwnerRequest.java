package com.rentalplatform.backend.owner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOwnerRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 100)
    private String businessName;

    @Size(max = 1000)
    private String description;
}
