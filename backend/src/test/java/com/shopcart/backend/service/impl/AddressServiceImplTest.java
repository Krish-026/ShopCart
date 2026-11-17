package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.AddressRequestDto;
import com.shopcart.backend.dto.AddressResponseDto;
import com.shopcart.backend.entity.Address;
import com.shopcart.backend.entity.User;
import com.shopcart.backend.exception.ResourceNotFoundException;
import com.shopcart.backend.repository.AddressRepository;
import com.shopcart.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void addAddress_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";

        AddressRequestDto request = new AddressRequestDto();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.addAddress(userEmail, request)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(addressRepository);
    }

    @Test
    void addAddress_ShouldSetDefaultTrue_WhenFirstAddressIsAdded() {
        // Arrange
        String userEmail = "john@example.com";

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        AddressRequestDto request = new AddressRequestDto();
        request.setFullName("John Doe");
        request.setPhoneNumber("9876543210");
        request.setStreetAddress("123 Main Street");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPostalCode("560001");
        request.setCountry("India");
        request.setIsDefault(false);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(List.of());

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> {
                    Address address = invocation.getArgument(0);
                    address.setId(1L);
                    return address;
                });

        // Act
        AddressResponseDto response = addressService.addAddress(userEmail, request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertTrue(response.getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByUserIdOrderByIsDefaultDescCreatedAtDesc(1l);
        verify(addressRepository).save(any(Address.class));

        verify(addressRepository, never()).resetDefaultAddressForUser(1L);
    }

    @Test
    void addAddress_ShouldNotSetDefault_WhenExistingAddressAndRequestIsNotDefault() {
        // Arrange
        String userEmail = "john@example.com";

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address existingAddress = Address.builder()
                .id(10L)
                .user(user)
                .isDefault(true)
                .build();

        AddressRequestDto request = new AddressRequestDto();
        request.setFullName("John Doe");
        request.setPhoneNumber("9876543210");
        request.setStreetAddress("456 New Street");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPostalCode("560002");
        request.setCountry("India");
        request.setIsDefault(false);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(List.of(existingAddress));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> {
                    Address address = invocation.getArgument(0);
                    address.setId(11L);
                    return address;
                });

        // Act
        AddressResponseDto response = addressService.addAddress(userEmail, request);

        // Assert
        assertNotNull(response);
        assertEquals(11L, response.getId());
        assertFalse(response.getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);
        verify(addressRepository).save(any(Address.class));

        verify(addressRepository, never()).resetDefaultAddressForUser(1L);
    }

    @Test
    void addAddress_ShouldResetExistingDefault_WhenNewAddressIsMarkedDefault() {
        // Arrange
        String userEmail = "john@example.com";

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address existingAddress = Address.builder()
                .id(10L)
                .user(user)
                .isDefault(true)
                .build();

        AddressRequestDto request = new AddressRequestDto();
        request.setFullName("John Doe");
        request.setPhoneNumber("9876543210");
        request.setStreetAddress("456 New Street");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPostalCode("560002");
        request.setCountry("India");
        request.setIsDefault(true);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(List.of(existingAddress));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> {
                    Address address = invocation.getArgument(0);
                    address.setId(11L);
                    return address;
                });

        // Act
        AddressResponseDto response = addressService.addAddress(userEmail, request);

        // Assert
        assertNotNull(response);
        assertEquals(11L, response.getId());
        assertTrue(response.getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);
        verify(addressRepository).resetDefaultAddressForUser(1L);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void getUserAddresses_ShouldReturnAddressList_WhenAddressesExist() {
        // Arrange
        String userEmail = "john@example.com";

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address address1 = Address.builder()
                .id(10L)
                .user(user)
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .streetAddress("123 Main Street")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560001")
                .country("India")
                .isDefault(true)
                .build();

        Address address2 = Address.builder()
                .id(11L)
                .user(user)
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .streetAddress("456 Second Street")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560002")
                .country("India")
                .isDefault(false)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(List.of(address1, address2));

        // Act
        List<AddressResponseDto> response = addressService.getUserAddresses(userEmail);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(10L, response.get(0).getId());
        assertEquals("123 Main Street", response.get(0).getStreetAddress());
        assertTrue(response.get(0).getIsDefault());

        assertEquals(11L, response.get(1).getId());
        assertEquals("456 Second Street", response.get(1).getStreetAddress());
        assertFalse(response.get(1).getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);
    }

    @Test
    void getUserAddresses_ShouldReturnEmptyList_WhenNoAddressesExist() {
        // Arrange
        String userEmail = "john@example.com";

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(List.of());

        // Act
        List<AddressResponseDto> response = addressService.getUserAddresses(userEmail);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);
    }

    @Test
    void getUserAddresses_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.getUserAddresses(userEmail)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(addressRepository);
    }

    @Test
    void getAddressById_ShouldReturnAddress_WhenAddressExists() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address address = Address.builder()
                .id(addressId)
                .user(user)
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .streetAddress("123 Main Street")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560001")
                .country("India")
                .isDefault(true)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.of(address));

        // Act
        AddressResponseDto response = addressService.getAddressById(userEmail, addressId);

        // Assert
        assertNotNull(response);
        assertEquals(addressId, response.getId());
        assertEquals("John Doe", response.getFullName());
        assertEquals("123 Main Street", response.getStreetAddress());
        assertEquals("Bengaluru", response.getCity());
        assertTrue(response.getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);
    }

    @Test
    void getAddressById_ShouldThrowException_WhenAddressDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.empty());

        // Act and Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.getAddressById(userEmail, addressId)
        );

        assertEquals(
                "Address not found with ID: " + addressId,
                exception.getMessage()
        );

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);
    }

    @Test
    void getAddressById_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.getAddressById(userEmail, addressId)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(addressRepository);
    }

    @Test
    void updateAddress_ShouldUpdateAddress_WhenAddressExists() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address address = Address.builder()
                .id(addressId)
                .user(user)
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .streetAddress("Old Street")
                .city("Delhi")
                .state("Delhi")
                .postalCode("110001")
                .country("India")
                .isDefault(true)
                .build();

        AddressRequestDto request = new AddressRequestDto();
        request.setFullName("John Smith");
        request.setPhoneNumber("9999999999");
        request.setStreetAddress("New Street");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPostalCode("560001");
        request.setCountry("India");
        request.setIsDefault(true);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1l))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AddressResponseDto response = addressService.updateAddress(userEmail, addressId, request);

        // Assert
        assertNotNull(response);
        assertEquals(addressId, response.getId());
        assertEquals("John Smith", response.getFullName());
        assertEquals("9999999999", response.getPhoneNumber());
        assertEquals("New Street", response.getStreetAddress());
        assertEquals("Bengaluru", response.getCity());
        assertEquals("Karnataka", response.getState());
        assertEquals("560001", response.getPostalCode());
        assertEquals("India", response.getCountry());
        assertTrue(response.getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);
        verify(addressRepository).save(address);

        verify(addressRepository, never()).resetDefaultAddressForUser(1L);

    }

    @Test
    void updateAddress_ShouldSetDefault_WhenNonDefaultAddressIsMarkedDefault() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address address = Address.builder()
                .id(addressId)
                .user(user)
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .streetAddress("Old Street")
                .city("Delhi")
                .state("Delhi")
                .postalCode("110001")
                .country("India")
                .isDefault(false)
                .build();

        AddressRequestDto request = new AddressRequestDto();
        request.setFullName("John Smith");
        request.setPhoneNumber("9999999999");
        request.setStreetAddress("New Street");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPostalCode("560001");
        request.setCountry("India");
        request.setIsDefault(true);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AddressResponseDto response =
                addressService.updateAddress(userEmail, addressId, request);

        // Assert
        assertNotNull(response);
        assertEquals(addressId, response.getId());
        assertEquals("John Smith", response.getFullName());
        assertEquals("New Street", response.getStreetAddress());
        assertTrue(response.getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);
        verify(addressRepository).resetDefaultAddressForUser(1L);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void updateAddress_ShouldThrowException_WhenAddressDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        AddressRequestDto request = new AddressRequestDto();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.updateAddress(userEmail, addressId, request)
        );

        assertEquals(
                "Address not found with ID: " + addressId,
                exception.getMessage()
        );

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);

        verify(addressRepository, never()).resetDefaultAddressForUser(1L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void updateAddress_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        AddressRequestDto request = new AddressRequestDto();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.updateAddress(userEmail, addressId, request)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(addressRepository);
    }

    @Test
    void updateAddress_ShouldKeepNonDefault_WhenDefaultStatusRemainsFalse() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address address = Address.builder()
                .id(addressId)
                .user(user)
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .streetAddress("Old Street")
                .city("Delhi")
                .state("Delhi")
                .postalCode("110001")
                .country("India")
                .isDefault(false)
                .build();

        AddressRequestDto request = new AddressRequestDto();
        request.setFullName("John Smith");
        request.setPhoneNumber("9999999999");
        request.setStreetAddress("New Street");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPostalCode("560001");
        request.setCountry("India");
        request.setIsDefault(false);

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AddressResponseDto response =
                addressService.updateAddress(userEmail, addressId, request);

        // Assert
        assertNotNull(response);
        assertEquals(addressId, response.getId());
        assertEquals("John Smith", response.getFullName());
        assertEquals("New Street", response.getStreetAddress());
        assertFalse(response.getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);
        verify(addressRepository).save(address);

        verify(addressRepository, never()).resetDefaultAddressForUser(1L);
    }

    @Test
    void setDefaultAddress_ShouldSetAddressAsDefault_WhenAddressExists() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address address = Address.builder()
                .id(addressId)
                .user(user)
                .fullName("John Doe")
                .phoneNumber("9876543210")
                .streetAddress("123 Main Street")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560001")
                .country("India")
                .isDefault(false)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.of(address));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AddressResponseDto response = addressService.setDefaultAddress(userEmail, addressId);

        // Assert
        assertNotNull(response);
        assertEquals(addressId, response.getId());
        assertTrue(response.getIsDefault());

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);
        verify(addressRepository).resetDefaultAddressForUser(1L);
        verify(addressRepository).save(any(Address.class));

    }

    @Test
    void setDefaultAddress_ShouldThrowException_WhenAddressDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.setDefaultAddress(userEmail, addressId)
        );

        assertEquals(
                "Address not found with ID: " + addressId,
                exception.getMessage()
        );

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);

        verify(addressRepository, never()).resetDefaultAddressForUser(1L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void setDefaultAddress_ShouldThrowException_WhenUserDoesNotExist() {
        String userEmail = "john@example.com";
        Long addressId = 1L;

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.setDefaultAddress(userEmail, addressId)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(addressRepository);
    }

    @Test
    void deleteAddress_ShouldDeleteAddress_WhenAddressIsNotDefault() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address address = Address.builder()
                .id(addressId)
                .user(user)
                .isDefault(false)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.of(address));

        // Act
        addressService.deleteAddress(userEmail, addressId);

        // Assert
        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);
        verify(addressRepository).delete(address);

        verify(addressRepository, never()).findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void deleteAddress_ShouldSetAnotherAddressAsDefault_WhenDefaultAddressIsDeleted() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address addressToDelete = Address.builder()
                .id(addressId)
                .user(user)
                .isDefault(true)
                .build();

        Address remainingAddress = Address.builder()
                .id(20L)
                .user(user)
                .isDefault(false)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.of(addressToDelete));

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(List.of(remainingAddress));

        when(addressRepository.save(any(Address.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        addressService.deleteAddress(userEmail, addressId);

        // Assert
        verify(addressRepository).delete(addressToDelete);
        verify(addressRepository).findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);
        assertTrue(remainingAddress.getIsDefault());
        verify(addressRepository).save(remainingAddress);
    }

    @Test
    void deleteAddress_ShouldNotPromoteAnyAddress_WhenOnlyAddressIsDeleted() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        Address address = Address.builder()
                .id(addressId)
                .user(user)
                .isDefault(true)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.of(address));

        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(List.of());

        // Act
        addressService.deleteAddress(userEmail, addressId);

        // Assert
        verify(addressRepository).delete(address);

        verify(addressRepository)
                .findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);

        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void deleteAddress_ShouldThrowException_WhenAddressDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        User user = User.builder()
                .id(1L)
                .email(userEmail)
                .build();

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(user));

        when(addressRepository.findByIdAndUserId(addressId, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.deleteAddress(userEmail, addressId)
        );

        assertEquals(
                "Address not found with ID: " + addressId,
                exception.getMessage()
        );

        verify(userRepository).findByEmail(userEmail);
        verify(addressRepository).findByIdAndUserId(addressId, 1L);

        verify(addressRepository, never()).delete(any(Address.class));

        verify(addressRepository, never())
                .findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L);
    }


    @Test
    void deleteAddress_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        String userEmail = "john@example.com";
        Long addressId = 10L;

        when(userRepository.findByEmail(userEmail))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> addressService.deleteAddress(userEmail, addressId)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(addressRepository);
    }

}
