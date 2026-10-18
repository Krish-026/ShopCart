package com.shopcart.backend.controller;

import com.shopcart.backend.dto.AddressRequestDto;
import com.shopcart.backend.dto.AddressResponseDto;
import com.shopcart.backend.response.ApiResponse;
import com.shopcart.backend.response.ResponseUtil;
import com.shopcart.backend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/addresses")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Add logged in user Address")
    public ResponseEntity<ApiResponse<AddressResponseDto>> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequestDto request) {

        AddressResponseDto response = addressService.addAddress(userDetails.getUsername(), request);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "User addresses retrieved successfully")
        );
    }

    @GetMapping
    @Operation(summary = "Get logged in user's all Addresses")
    public ResponseEntity<ApiResponse<List<AddressResponseDto>>> getUserAddresses(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        List<AddressResponseDto> response = addressService.getUserAddresses(userDetails.getUsername());
        return ResponseEntity.ok(
                ResponseUtil.success(response, "User addresses retrieved successfully")
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get logged in user's Address by ID")
    public ResponseEntity<ApiResponse<AddressResponseDto>> getAddressById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ){
        AddressResponseDto response = addressService.getAddressById(userDetails.getUsername(), id);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "Address retrieved successfully")
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update logged in user's Address")
    public ResponseEntity<ApiResponse<AddressResponseDto>> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDto request
    ){
        AddressResponseDto response = addressService.updateAddress(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "Address updated successfully")
        );
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "Update default address")
    public ResponseEntity<ApiResponse<AddressResponseDto>> setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ){
        AddressResponseDto response = addressService.setDefaultAddress(userDetails.getUsername(), id);
        return ResponseEntity.ok(
                ResponseUtil.success(response, "Default address set successfully")
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete currently logged in user Address by ID")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ){
        addressService.deleteAddress(userDetails.getUsername(), id);
        return ResponseEntity.ok(
                ResponseUtil.success(null, "Address deleted successfully")
        );
    }
}
