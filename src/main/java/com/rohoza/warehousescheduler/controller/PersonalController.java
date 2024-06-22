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
import java.util.List;
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

        String workingDays = daysToString(preferences.getWorkingDays());
        String possibleDays = daysToString(preferences.getPossibleDays());
        String workingLocations = locationsToString(preferences.getWorkingLocations());
        String possibleLocations = locationsToString(preferences.getPossibleLocations());

        model.addAttribute("worker", worker);
        model.addAttribute("workerName", worker.getFirstName() + " " + worker.getLastName());
        model.addAttribute("workingDays", workingDays);
        model.addAttribute("possibleDays", possibleDays);
        model.addAttribute("workingLocations", workingLocations);
        model.addAttribute("possibleLocations", possibleLocations);

        return "personal-data";
    }

    private String daysToString(List<DayOfWeek> days) {
        return days.stream().map(DayOfWeek::name).collect(Collectors.joining(", "));
    }

    private String locationsToString(List<Location> locations) {
        return locations.stream().map(Location::getName).collect(Collectors.joining(", "));
    }
}
