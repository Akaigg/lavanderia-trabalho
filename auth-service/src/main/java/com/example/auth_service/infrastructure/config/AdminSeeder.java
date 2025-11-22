package com.example.auth_service.infrastructure.config;

import com.example.auth_service.application.ports.PasswordHasher;
import com.example.auth_service.domain.user.User;
import com.example.auth_service.domain.user.UserRepository;
import com.example.auth_service.domain.user.vo.Email;
import com.example.auth_service.domain.user.vo.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {
            String adminEmail = "admin@lavanderia.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                String hash = passwordHasher.hash("admin123");
                User admin = new User("Administrador", hash, Email.of(adminEmail), RoleType.ADMIN);
                userRepository.save(admin);
                System.out.println("--- ADMIN CRIADO: admin@lavanderia.com / admin123 ---");
            }
        };
    }
}