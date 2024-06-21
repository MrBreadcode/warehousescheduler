package com.rohoza.warehousescheduler.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Entity
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;

    @OneToMany(mappedBy = "location")
    private List<Task> tasks;

    @ElementCollection
    private Map<Location, Duration> distanceToOtherLocations;

    public Location() {}

    public Location(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public Map<Location, Duration> getDistanceToOtherLocations() {
        return distanceToOtherLocations;
    }

    public void setDistanceToOtherLocations(Map<Location, Duration> distanceToOtherLocations) {
        this.distanceToOtherLocations = distanceToOtherLocations;
    }

    public Duration getDistanceTo(Location otherLocation) {
        return distanceToOtherLocations.getOrDefault(otherLocation, Duration.ZERO);
    }
}
