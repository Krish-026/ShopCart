package com.shopcart.backend.component;

import com.shopcart.backend.entity.Role;
import com.shopcart.backend.repository.RoleRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Builder
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception{
        if(roleRepository.findByName("ROLE_CUSTOMER").isEmpty()){
            Role customerRole = Role.builder()
                    .name("ROLE_CUSTOMER")
                    .build();
            roleRepository.save(customerRole);
            log.info("Seeded ROLE_CUSTOMER into the database.");
        }

        if(roleRepository.findByName("ROLE_ADMIN").isEmpty()){
            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .build();
            roleRepository.save(adminRole);
            log.info("Seeded ROLE_ADMIN into the database.");
        }
    }
}
