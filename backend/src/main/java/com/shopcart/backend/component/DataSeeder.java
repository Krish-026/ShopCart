package com.shopcart.backend.component;

import com.shopcart.backend.entity.Role;
import com.shopcart.backend.entity.User;
import com.shopcart.backend.repository.RoleRepository;
import com.shopcart.backend.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception{
// 1. Seed Roles
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("ROLE_CUSTOMER").build();
                    Role saved = roleRepository.save(role);
                    log.info("Seeded ROLE_CUSTOMER into database.");
                    return saved;
                });

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role role = Role.builder().name("ROLE_ADMIN").build();
                    Role saved = roleRepository.save(role);
                    log.info("Seeded ROLE_ADMIN into database.");
                    return saved;
                });

        // 2. Seed Default Admin User (for immediate testing after DB wipes)
        if (userRepository.findByEmail("admin@shopcart.com").isEmpty()) {
            User defaultAdmin = User.builder()
                    .email("admin@shopcart.com")
                    .password(passwordEncoder.encode("Admin@1234"))
                    .roles(Set.of(adminRole, customerRole))
                    .build();


            userRepository.save(defaultAdmin);
            log.info("Seeded default admin user: admin@shopcart.com / Admin@1234");
        }

    }
}
