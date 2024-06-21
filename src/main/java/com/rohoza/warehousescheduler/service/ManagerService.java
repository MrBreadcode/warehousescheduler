package com.rohoza.warehousescheduler.service;

import com.rohoza.warehousescheduler.model.Manager;
import com.rohoza.warehousescheduler.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ManagerService(ManagerRepository managerRepository, PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Manager> findAll() {
        return managerRepository.findAll();
    }

    public Manager findById(Long id) {
        return managerRepository.findById(id).orElse(null);
    }

    public Manager save(Manager manager) {
        if (manager.getId() != null) {
            Manager existingManager = findById(manager.getId());
            if (existingManager != null && !manager.getPassword().equals("")) {
                manager.setPassword(passwordEncoder.encode(manager.getPassword()));
            } else {
                manager.setPassword(existingManager.getPassword());
            }
        } else {
            manager.setPassword(passwordEncoder.encode(manager.getPassword()));
        }
        return managerRepository.save(manager);
    }

    public void deleteById(Long id) {
        managerRepository.deleteById(id);
    }
}
