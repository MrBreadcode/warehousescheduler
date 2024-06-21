package com.rohoza.warehousescheduler.service;

import com.rohoza.warehousescheduler.model.Admin;
import com.rohoza.warehousescheduler.model.Manager;
import com.rohoza.warehousescheduler.model.Worker;
import com.rohoza.warehousescheduler.repository.AdminRepository;
import com.rohoza.warehousescheduler.repository.ManagerRepository;
import com.rohoza.warehousescheduler.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final ManagerRepository managerRepository;
    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(AdminRepository adminRepository, ManagerRepository managerRepository, WorkerRepository workerRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.managerRepository = managerRepository;
        this.workerRepository = workerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Admin methods
    public List<Admin> findAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin findAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public Admin saveAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public void deleteAdminById(Long id) {
        adminRepository.deleteById(id);
    }

    // Manager methods
    public List<Manager> findAllManagers() {
        return managerRepository.findAll();
    }

    public Manager findManagerById(Long id) {
        return managerRepository.findById(id).orElse(null);
    }

    public Manager saveManager(Manager manager) {
        manager.setPassword(passwordEncoder.encode(manager.getPassword()));
        return managerRepository.save(manager);
    }

    public void deleteManagerById(Long id) {
        managerRepository.deleteById(id);
    }

    // Worker methods
    public List<Worker> findAllWorkers() {
        return workerRepository.findAll();
    }

    public Worker findWorkerById(Long id) {
        return workerRepository.findById(id).orElse(null);
    }

    public Worker saveWorker(Worker worker) {
        worker.setPassword(passwordEncoder.encode(worker.getPassword()));
        return workerRepository.save(worker);
    }

    public void deleteWorkerById(Long id) {
        workerRepository.deleteById(id);
    }
}
