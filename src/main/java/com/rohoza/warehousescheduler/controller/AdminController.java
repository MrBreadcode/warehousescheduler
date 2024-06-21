package com.rohoza.warehousescheduler.controller;

import com.rohoza.warehousescheduler.model.Admin;
import com.rohoza.warehousescheduler.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admins")
    public List<Admin> getAllAdmins() {
        return adminService.findAllAdmins();
    }

    @GetMapping("/admins/{id}")
    public Admin getAdminById(@PathVariable Long id) {
        return adminService.findAdminById(id);
    }

    @PostMapping("/admins")
    public Admin createAdmin(@RequestBody Admin admin) {
        return adminService.saveAdmin(admin);
    }

    @PutMapping("/admins/{id}")
    public Admin updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        admin.setId(id);
        return adminService.saveAdmin(admin);
    }

    @DeleteMapping("/admins/{id}")
    public void deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdminById(id);
    }
}
