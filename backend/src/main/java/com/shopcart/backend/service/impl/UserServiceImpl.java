package com.shopcart.backend.service.impl;

import com.shopcart.backend.dto.UserRegistrationDto;
import com.shopcart.backend.entity.Role;
import com.shopcart.backend.entity.User;
import com.shopcart.backend.repository.RoleRepository;
import com.shopcart.backend.repository.UserRepository;
import com.shopcart.backend.service.UserService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException(("Email is already registered!"));
        }

        Role customerRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Error: Default role not found in database;"));

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // We'll hash this later
                .roles(Set.of(customerRole))
                .build();
        return userRepository.save(user);
    }
}
