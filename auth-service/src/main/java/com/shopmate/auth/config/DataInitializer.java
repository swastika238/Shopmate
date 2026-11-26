package com.shopmate.auth.config;

import com.shopmate.auth.model.Role;
import com.shopmate.auth.model.User;
import com.shopmate.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedDefaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@shopmate.com")) {
                User admin = new User(
                        "ShopMate Admin",
                        "admin@shopmate.com",
                        passwordEncoder.encode("Admin@123"),
                        Role.ADMIN
                );
                userRepository.save(admin);
                log.info("Seeded default ADMIN user: admin@shopmate.com / Admin@123");
            }
        };
    }
}
