package com.employeemanagement.config;

import com.employeemanagement.entity.User;
import com.employeemanagement.entity.UserStatus;
import com.employeemanagement.entity.UserType;
import com.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createSuperAdmin(
                "Super Administrator 1",
                "admin1@employee-management.com",
                "Admin123!"
        );

        createSuperAdmin(
                "Super Administrator 2",
                "admin2@employee-management.com",
                "Admin123!"
        );
    }

    private void createSuperAdmin(
            String name,
            String email,
            String password
    ) {

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserType(UserType.SUPER_ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        user.setPermissions(Set.of(
                com.employeemanagement.entity.Permission.CREATE,
                com.employeemanagement.entity.Permission.READ,
                com.employeemanagement.entity.Permission.UPDATE,
                com.employeemanagement.entity.Permission.DELETE
        ));

        userRepository.save(user);
    }
}