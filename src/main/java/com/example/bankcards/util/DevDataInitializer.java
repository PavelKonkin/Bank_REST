package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("No admin user found. Creating test admin...");
            var admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRole(UserRole.ROLE_ADMIN);
            userRepository.save(admin);
            log.info("Test admin created successfully.");
        } else {
            log.info("Test admin already exist.");
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            log.info("No user found. Creating test user...");
            var user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRole(UserRole.ROLE_USER);
            userRepository.save(user);
            log.info("Test user created successfully.");
        } else {
            log.info("Test user already exist.");
        }
    }
}
