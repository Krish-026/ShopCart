package com.shopcart.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponseDto {

    private Long id;
    private String fullName;
    private String phoneNumber;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
