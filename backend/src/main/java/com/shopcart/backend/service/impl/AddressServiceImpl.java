package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.AddressRequestDto;
import com.shopcart.backend.dto.AddressResponseDto;
import com.shopcart.backend.entity.Address;
import com.shopcart.backend.entity.User;
import com.shopcart.backend.exception.ErrorCode;
import com.shopcart.backend.exception.ResourceNotFoundException;
import com.shopcart.backend.repository.AddressRepository;
import com.shopcart.backend.repository.UserRepository;
import com.shopcart.backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressResponseDto addAddress(String userEmail, AddressRequestDto request) {
        User user = getUser(userEmail);
        // If this is the user's first address or marked as default, reset other defaults
        List<Address> existing = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
        boolean shouldBeDefault = Boolean.TRUE.equals(request.getIsDefault() || existing.isEmpty());

        if(shouldBeDefault && !existing.isEmpty()){
            addressRepository.resetDefaultAddressForUser(user.getId());
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .streetAddress(request.getStreetAddress())
                .postalCode(request.getPostalCode())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .isDefault(shouldBeDefault)
                .build();

        Address saved = addressRepository.save(address);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDto> getUserAddresses(String userEmail) {
        User user = getUser(userEmail);
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDto getAddressById(String userEmail, Long addressId) {
        User user = getUser(userEmail);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Address not found with ID: " + addressId));

        return mapToDto(address);
    }

    @Override
    @Transactional
    public AddressResponseDto updateAddress(String userEmail, Long addressId, AddressRequestDto request) {
        User user = getUser(userEmail);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(()-> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Address not found with ID: " + addressId));

        if(Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())){
            addressRepository.resetDefaultAddressForUser(user.getId());
            address.setIsDefault(true);
        }

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());

        Address updated = addressRepository.save(address);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public AddressResponseDto setDefaultAddress(String userEmail, Long addressId) {
        User user = getUser(userEmail);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Address not found with ID: " + addressId));
        addressRepository.resetDefaultAddressForUser(user.getId());
        address.setIsDefault(true);
        Address updated = addressRepository.save(address);
        return mapToDto(address);
    }

    @Override
    @Transactional
    public void deleteAddress(String userEmail, Long addressId) {
        User user = getUser(userEmail);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.INVALID_INPUT, "Address not found with ID: " + addressId));
        addressRepository.delete(address);

        // If deleted address was default, promote the newest remaining address to default
        if(Boolean.TRUE.equals(address.getIsDefault())){
            List<Address> remainingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());
            if(!remainingAddresses.isEmpty()){
                Address nextDefault = remainingAddresses.get(0);
                nextDefault.setIsDefault(true);
                addressRepository.save(nextDefault);
            }
        }
    }

    private User getUser(String userEmail){
        return userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new ResourceNotFoundException(ErrorCode.UNAUTHORIZED_ACCESS, "User not found"));
    }

    private AddressResponseDto mapToDto(Address address){
        return AddressResponseDto.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .streetAddress(address.getStreetAddress())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updateAt(address.getUpdatedAt())
                .build();
    }
}
