package com.rohoza.warehousescheduler.config;

import com.rohoza.warehousescheduler.model.Admin;
import com.rohoza.warehousescheduler.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;

@Configuration
public class DataInitializer {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin("admin", passwordEncoder.encode("admin"));
            adminRepository.save(admin);
        }
    }
}
