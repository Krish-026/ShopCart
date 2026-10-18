package com.shopcart.backend.service;

import com.shopcart.backend.dto.AddressRequestDto;
import com.shopcart.backend.dto.AddressResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AddressService {
    AddressResponseDto addAddress(String userEmail, AddressRequestDto request);
    List<AddressResponseDto> getUserAddresses(String userEmail);
    AddressResponseDto getAddressById(String userEmail, Long addressId);
    AddressResponseDto updateAddress(String userEmail, Long addressId, AddressRequestDto request);
    AddressResponseDto setDefaultAddress(String userEmail, Long addressId);
    void deleteAddress(String userEmail, Long addressId);
}
