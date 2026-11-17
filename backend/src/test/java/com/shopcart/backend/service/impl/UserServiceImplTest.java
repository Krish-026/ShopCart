package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.UserRegistrationDto;
import com.shopcart.backend.entity.Role;
import com.shopcart.backend.entity.User;
import com.shopcart.backend.repository.RoleRepository;
import com.shopcart.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_ShouldReturnSavedUser_WhenRegistrationIsValid() {
        // Arrange
        UserRegistrationDto request = new UserRegistrationDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        Role customerRole = Role.builder()
                .id(1L)
                .name("ROLE_CUSTOMER")
                .build();

        String encodedPassword = "$2a$10$encodedPassword";

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_CUSTOMER"))
                .thenReturn(Optional.of(customerRole));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        when(passwordEncoder.encode("password123"))
                .thenReturn(encodedPassword);

        // Act
        User response = userService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(encodedPassword, response.getPassword());
        assertTrue(response.getRoles().contains(customerRole));


        verify(passwordEncoder).encode("password123");
        verify(userRepository).existsByEmail("john@example.com");
        verify(roleRepository).findByName("ROLE_CUSTOMER");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(customerRole));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        UserRegistrationDto request = new UserRegistrationDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("Email is already registered!", exception.getMessage());

        verify(userRepository).existsByEmail("john@example.com");

        verify(roleRepository, never()).findByName("ROLE_CUSTOMER");
        verify(passwordEncoder, never()).encode("password123");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenCustomerRoleNotFound() {
        // Arrange
        UserRegistrationDto request = new UserRegistrationDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_CUSTOMER"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(request)
        );

        assertEquals(
                "Error: Default role not found in database;",
                exception.getMessage()
        );

        // Verify interactions
        verify(userRepository).existsByEmail("john@example.com");
        verify(roleRepository).findByName("ROLE_CUSTOMER");

        verify(passwordEncoder, never()).encode("password123");
        verify(userRepository, never()).save(any(User.class));
    }
}
