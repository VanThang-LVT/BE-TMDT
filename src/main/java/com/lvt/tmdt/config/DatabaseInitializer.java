package com.lvt.tmdt.config;

import com.lvt.tmdt.entity.Role;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.enums.UserStatus;
import com.lvt.tmdt.repository.RoleRepository;
import com.lvt.tmdt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("CUSTOMER").description("Customer Role").build()));

        Role sellerRole = roleRepository.findByRoleName("SELLER")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("SELLER").description("Seller Role").build()));

        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("ADMIN").description("Admin Role").build()));

        if (!userRepository.findByEmail("admin@tmdt.com").isPresent()) {
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User adminUser = User.builder()
                    .fullName("Quản trị hệ thống")
                    .email("admin@tmdt.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("0000000000")
                    .status(UserStatus.ACTIVE)
                    .roles(roles)
                    .build();

            userRepository.save(adminUser);
        }
    }
}
