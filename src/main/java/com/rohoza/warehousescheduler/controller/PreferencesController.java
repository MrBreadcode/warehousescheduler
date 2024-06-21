package com.rohoza.warehousescheduler.controller;

import com.rohoza.warehousescheduler.model.Location;
import com.rohoza.warehousescheduler.model.Preferences;
import com.rohoza.warehousescheduler.model.Worker;
import com.rohoza.warehousescheduler.service.LocationService;
import com.rohoza.warehousescheduler.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/preferences")
public class PreferencesController {

    private final WorkerService workerService;
    private final LocationService locationService;

    @Autowired
    public PreferencesController(WorkerService workerService, LocationService locationService) {
        this.workerService = workerService;
        this.locationService = locationService;
    }

    @GetMapping
    public String getAllWorkers(Model model) {
        List<Worker> workers = workerService.findAll();
        workers.sort(Comparator.comparing(Worker::getFirstName)
                .thenComparing(Worker::getLastName));
        model.addAttribute("workers", workers);
        return "preferences-list";
    }

    @GetMapping("/show/{id}")
    public String showPreferences(@PathVariable("id") Long id, Model model) {
        Worker worker = workerService.findById(id);
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

        model.addAttribute("worker", worker);
        model.addAttribute("workingDays", workingDays);
        model.addAttribute("possibleDays", possibleDays);
        model.addAttribute("workingLocations", workingLocations);
        model.addAttribute("possibleLocations", possibleLocations);

        return "show-preferences";
    }

    @GetMapping("/edit/{id}")
    public String showEditPreferencesForm(@PathVariable("id") Long id, Model model) {
        Worker worker = workerService.findById(id);
        model.addAttribute("worker", worker);
        model.addAttribute("allLocations", locationService.findAll());
        return "edit-preferences";
    }

    @PostMapping("/edit/{id}")
    public String updatePreferences(@PathVariable Long id,
                                    @RequestParam("shiftPreference") Preferences.ShiftType shiftPreference,
                                    @RequestParam(value = "workingDays", required = false, defaultValue = "") List<String> workingDays,
                                    @RequestParam(value = "workingLocations", required = false, defaultValue = "") List<Long> workingLocations,
                                    @RequestParam(value = "possibleDays", required = false, defaultValue = "") List<String> possibleDays,
                                    @RequestParam(value = "possibleLocations", required = false, defaultValue = "") List<Long> possibleLocations,
                                    @RequestParam("vacation") boolean vacation,
                                    RedirectAttributes redirectAttributes) {
        if (workingDays.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose at least one working day");
            return "redirect:/api/preferences/edit/" + id;
        }
        if (workingLocations.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose at least one working location");
            return "redirect:/api/preferences/edit/" + id;
        }
        Worker worker = workerService.findById(id);
        Preferences preferences = worker.getPreferences();
        preferences.setShiftPreference(shiftPreference);
        preferences.setWorkingDays(workingDays.stream().map(DayOfWeek::valueOf).collect(Collectors.toList()));
        preferences.setWorkingLocations(workingLocations.stream().map(locationService::findById).collect(Collectors.toList()));
        preferences.setPossibleDays(possibleDays.stream().map(DayOfWeek::valueOf).collect(Collectors.toList()));
        preferences.setPossibleLocations(possibleLocations.stream().map(locationService::findById).collect(Collectors.toList()));
        preferences.setVacation(vacation);
        workerService.savePreferences(worker);
        return "redirect:/api/preferences";
    }
}
