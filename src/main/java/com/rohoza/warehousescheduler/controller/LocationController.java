package com.rohoza.warehousescheduler.controller;

import com.rohoza.warehousescheduler.model.Location;
import com.rohoza.warehousescheduler.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public String getAllLocations(Model model) {
        List<Location> locations = locationService.findAll();
        model.addAttribute("locations", locations);
        return "location-list";
    }

    @GetMapping("/add-location")
    public String showCreateLocationForm(Model model) {
        List<Location> allLocations = locationService.findAll();
        model.addAttribute("location", new Location());
        model.addAttribute("allLocations", allLocations);
        return "add-location";
    }

    @PostMapping("/add-location")
    public String createLocation(@RequestParam Map<String, String> distances, @ModelAttribute Location location) {
        Map<Location, Duration> distanceMap = processDistances(distances, location.getId());
        location.setDistanceToOtherLocations(distanceMap);
        locationService.save(location);
        return "redirect:/api/locations";
    }

    @GetMapping("/show-location/{id}")
    public String showLocationDetails(@PathVariable("id") Long id, Model model) {
        Location location = locationService.findById(id);
        List<Location> allLocations = locationService.findAll().stream()
                .filter(loc -> !loc.getId().equals(id))
                .collect(Collectors.toList());

        // Конвертуємо Duration в формат HH:mm для відображення у формі
        Map<Long, String> distanceMap = new HashMap<>();
        for (Location loc : allLocations) {
            Duration duration = location.getDistanceToOtherLocations().get(loc);
            if (duration != null) {
                distanceMap.put(loc.getId(), durationToString(duration));
            }
        }

        model.addAttribute("location", location);
        model.addAttribute("allLocations", allLocations);
        model.addAttribute("distanceMap", distanceMap);

        return "show-location";
    }

    @GetMapping("/edit-location/{id}")
    public String showEditLocationForm(@PathVariable("id") Long id, Model model) {
        Location location = locationService.findById(id);
        List<Location> allLocations = locationService.findAll().stream()
                .filter(loc -> !loc.getId().equals(id))
                .collect(Collectors.toList());

        // Конвертуємо Duration в формат HH:mm для відображення у формі
        Map<Long, String> distanceMap = new HashMap<>();
        for (Location loc : allLocations) {
            Duration duration = location.getDistanceToOtherLocations().get(loc);
            if (duration != null) {
                distanceMap.put(loc.getId(), durationToString(duration));
            }
        }

        model.addAttribute("location", location);
        model.addAttribute("allLocations", allLocations);
        model.addAttribute("distanceMap", distanceMap);

        return "edit-location";
    }

    @PostMapping("/edit-location/{id}")
    public String updateLocation(@PathVariable Long id, @RequestParam Map<String, String> distances, @ModelAttribute Location location) {
        Map<Location, Duration> distanceMap = processDistances(distances, id);
        location.setDistanceToOtherLocations(distanceMap);
        locationService.save(location);
        return "redirect:/api/locations";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Location getLocationById(@PathVariable Long id) {
        return locationService.findById(id);
    }

    @GetMapping("/delete-location/{id}")
    public String deleteLocation(@PathVariable Long id) {
        locationService.deleteById(id);
        return "redirect:/api/locations";
    }

    private Map<Location, Duration> processDistances(Map<String, String> distances, Long locationId) {
        Map<Location, Duration> distanceMap = new HashMap<>();
        List<Location> allLocations = locationService.findAll();
        for (Location loc : allLocations) {
            if (!loc.getId().equals(locationId)) {
                String distanceStr = distances.get("distances[" + loc.getId() + "]");
                if (distanceStr != null && !distanceStr.isEmpty()) {
                    try {
                        distanceMap.put(loc, stringToDuration(distanceStr));
                    } catch (Exception e) {
                        // handle invalid format
                    }
                }
            }
        }
        return distanceMap;
    }

    private String durationToString(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private Duration stringToDuration(String durationString) {
        LocalTime time = LocalTime.parse(durationString, TIME_FORMATTER);
        return Duration.ofHours(time.getHour()).plusMinutes(time.getMinute());
    }
}
