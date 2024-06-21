package com.rohoza.warehousescheduler.controller;

import com.rohoza.warehousescheduler.model.Manager;
import com.rohoza.warehousescheduler.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/managers")
public class ManagerController {

    private final ManagerService managerService;

    @Autowired
    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping
    public String getAllManagers(Model model) {
        List<Manager> managers = managerService.findAll();
        model.addAttribute("managers", managers);
        return "manager-list";
    }

    @GetMapping("/add-manager")
    public String showCreateManagerForm(Model model) {
        model.addAttribute("manager", new Manager());
        return "add-manager";
    }

    @PostMapping("/add-manager")
    public String createManager(@ModelAttribute Manager manager) {
        managerService.save(manager);
        return "redirect:/api/managers";
    }

    @GetMapping("/edit-manager/{id}")
    public String showEditManagerForm(@PathVariable Long id, Model model) {
        Manager manager = managerService.findById(id);
        model.addAttribute("manager", manager);
        return "edit-manager";
    }

    @PostMapping("/edit-manager/{id}")
    public String updateManager(@PathVariable Long id, @ModelAttribute Manager manager) {
        manager.setId(id);
        managerService.save(manager);
        return "redirect:/api/managers";
    }

    @GetMapping("/delete-manager/{id}")
    public String deleteManager(@PathVariable Long id) {
        managerService.deleteById(id);
        return "redirect:/api/managers";
    }
}
