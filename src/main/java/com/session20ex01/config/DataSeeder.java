package com.session20ex01.config;

import com.session20ex01.entity.Employee;
import com.session20ex01.entity.Role;
import com.session20ex01.repository.EmployeeRepository;
import com.session20ex01.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            return;
        }

        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        Role userRole = Role.builder()
                .name("ROLE_USER")
                .build();

        roleRepository.save(adminRole);
        roleRepository.save(userRole);

        Employee admin = Employee.builder()
                .username("admin")
                .password(passwordEncoder.encode("123456"))
                .active(true)
                .roles(Set.of(adminRole, userRole))
                .build();

        Employee user = Employee.builder()
                .username("employee")
                .password(passwordEncoder.encode("123456"))
                .active(true)
                .roles(Set.of(userRole))
                .build();

        employeeRepository.save(admin);
        employeeRepository.save(user);
    }
}
