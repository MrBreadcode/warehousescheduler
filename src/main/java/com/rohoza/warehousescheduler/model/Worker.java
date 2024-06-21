package com.rohoza.warehousescheduler.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Worker extends User {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    @Embedded
    private Preferences preferences;

    @ManyToMany(mappedBy = "assignedWorkers")
    private List<Task> tasks;

    public Worker() {
        super();
        setRole("WORKER");
    }

    public Worker(String username, String password, String firstName, String lastName, String email, String phoneNumber, Preferences preferences) {
        super(username, password, "WORKER");
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.preferences = preferences;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Task> getCurrentTasks() {
        LocalDateTime now = LocalDateTime.now();
        return tasks.stream()
                .filter(task -> task.getFinishTime().isAfter(now))
                .collect(Collectors.toList());
    }

    public List<Task> getArchiveTasks() {
        LocalDateTime now = LocalDateTime.now();
        return tasks.stream()
                .filter(task -> task.getFinishTime().isBefore(now))
                .collect(Collectors.toList());
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> findTasksByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDate fromLocalDate = fromDate.toLocalDate();
        LocalDate toLocalDate = toDate.toLocalDate();

        return tasks.stream()
                .filter(task -> {
                    LocalDate taskStartDate = task.getStartTime().toLocalDate();
                    LocalDate taskFinishDate = task.getFinishTime().toLocalDate();
                    return !taskStartDate.isBefore(fromLocalDate) && !taskFinishDate.isAfter(toLocalDate);
                })
                .collect(Collectors.toList());
    }

    public List<Task> findCurrentTasksByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime now = LocalDateTime.now();
        return findTasksByDateRange(fromDate, toDate).stream()
                .filter(task -> task.getFinishTime().isAfter(now))
                .collect(Collectors.toList());
    }

    public List<Task> findArchiveTasksByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime now = LocalDateTime.now();
        return findTasksByDateRange(fromDate, toDate).stream()
                .filter(task -> task.getFinishTime().isBefore(now))
                .collect(Collectors.toList());
    }

    public Integer getTaskDislike(Task task) {
        return Math.max(preferences.getStartTimeDislike(task.getStartTime()),
                preferences.getFinishTimeDislike(task.getFinishTime())) +
                preferences.getLocationDislike(task.getLocation());
    }

    public boolean canWorkOnTask(Task task) {
        if (!canWorkOnTaskIgnoringAll(task)) {
            return false;
        }

        // Check if the task conflicts with any of the worker's existing tasks
        for (Task existingTask : getTasks()) {
            if (task.conflictsWith(existingTask)) {
                return false;
            }
        }

        return true;
    }

    public boolean canWorkOnTaskIgnoringOne(Task task, Task ignoring) {
        if (!canWorkOnTaskIgnoringAll(task)) {
            return false;
        }

        // Check if the task conflicts with any of the worker's existing tasks except the one to ignore
        for (Task existingTask : getTasks()) {
            if (existingTask != ignoring) {
                if (task.conflictsWith(existingTask)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean canWorkOnTaskIgnoringAll(Task task) {
        // Check if worker is on vacation
        if (preferences.isVacation()) {
            return false;
        }

        // Check if worker can work at the start and finish times
        if (!preferences.canWorkAtTime(task.getStartTime()) || !preferences.canWorkAtTime(task.getFinishTime())) {
            return false;
        }

        // Check if worker can work at the task's location
        Location location = task.getLocation();
        if (!preferences.canWorkAtLocation(location)) {
            return false;
        }

        // Check if the worker is in the declined workers list for the task
        if (task.getDeclinedWorkers().contains(this)) {
            return false;
        }

        return true;
    }
}
