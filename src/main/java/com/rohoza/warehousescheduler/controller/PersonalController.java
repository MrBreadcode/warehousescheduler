package com.rohoza.warehousescheduler.controller;

import com.rohoza.warehousescheduler.model.Location;
import com.rohoza.warehousescheduler.model.Preferences;
import com.rohoza.warehousescheduler.model.Worker;
import com.rohoza.warehousescheduler.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.DayOfWeek;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/personal")
public class PersonalController {

    private final WorkerService workerService;

    @Autowired
    public PersonalController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    public String getPersonal(Model model, Principal principal) {
        Worker worker = workerService.findByUsername(principal.getName());
        Preferences preferences = worker.getPreferences();

        String workingDays = preferences.getWorkingDays().stream()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(", "));

        String possibleDays = preferences.getPossibleDays().stream()
                .map(DayOfWeek::name)
                .collect(Collectors.joining(", "));

        String workingLocations = preferences.getWorkingLocations().stream()
                .map(Location::getName)
                .collect(Collectors.joining(", "));

        String possibleLocations = preferences.getPossibleLocations().stream()
                .map(Location::getName)
                .collect(Collectors.joining(", "));

        model.addAttribute("workerName", worker.getFirstName() + " " + worker.getLastName());
        model.addAttribute("worker", worker);
        model.addAttribute("workingDays", workingDays);
        model.addAttribute("possibleDays", possibleDays);
        model.addAttribute("workingLocations", workingLocations);
        model.addAttribute("possibleLocations", possibleLocations);
        return "personal-data";
    }
}
