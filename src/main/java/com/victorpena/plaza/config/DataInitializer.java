package com.victorpena.plaza.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.victorpena.plaza.model.Role;
import com.victorpena.plaza.model.User;
import com.victorpena.plaza.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.count() == 0) {

                User admin = new User();
                admin.setFirstName("Victor");
                admin.setLastName("Pena");
                admin.setEmail("admin@penaplaza.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);

                userRepository.save(admin);

                System.out.println("Admin user created.");
            }
        };
    }
}