package com.rohoza.warehousescheduler.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private Duration realTime;
    private int neededWorkers;
    private boolean assigned;
    private boolean manually;

    @ManyToOne
    private Location location;

    @ManyToMany
    private List<Worker> declinedWorkers = new ArrayList<>();

    @ManyToMany
    private List<Worker> assignedWorkers = new ArrayList<>();

    public Task() {}

    public Task(String description, LocalDateTime startTime, LocalDateTime finishTime, int neededWorkers, Location location) {
        this.description = description;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.location = location;
        this.assigned = false;
        this.manually = false;
        this.realTime = Duration.between(startTime, finishTime);
        this.neededWorkers = neededWorkers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.realTime = Duration.between(startTime, finishTime);
        unAssign();
        unDecline();
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
        this.realTime = Duration.between(startTime, finishTime);
        unAssign();
        unDecline();
    }

    public Duration getRealTime() {
        return realTime;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void unAssign() {
        for (Worker worker : assignedWorkers) {
            worker.getTasks().remove(this);
        }
        this.assignedWorkers.clear();
        this.assigned = false;
        this.manually = false;
    }

    public boolean isManually() {
        return manually;
    }

    public List<Worker> getDeclinedWorkers() {
        return declinedWorkers;
    }

    public void declineWorker(Worker worker) {
        this.declinedWorkers.add(worker);
        unAssign();
    }

    public void unDecline() {
        this.declinedWorkers.clear();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        unDecline();
    }

    public List<Worker> getAssignedWorkers() {
        return assignedWorkers;
    }

    public void assignWorkers(List<Worker> assignedWorkers) {
        this.unAssign();
        this.assignedWorkers.addAll(assignedWorkers);
        this.assigned = true;
        for (Worker worker : assignedWorkers) {
            worker.getTasks().add(this);
        }
    }

    public void assignManually(List<Worker> assignedWorkers) {
        this.unAssign();
        this.assignedWorkers.addAll(assignedWorkers);
        this.assigned = true;
        this.manually = true;
        for (Worker worker : assignedWorkers) {
            worker.getTasks().add(this);
        }
    }

    public int getNeededWorkers() {
        return neededWorkers;
    }

    public void setNeededWorkers(int neededWorkers) {
        this.neededWorkers = neededWorkers;
    }

    public boolean conflictsWith(Task task) {
        LocalDateTime beginTime1 = this.getStartTime();
        LocalDateTime endTime1 = this.getFinishTime();
        LocalDateTime beginTime2 = task.getStartTime();
        LocalDateTime endTime2 = task.getFinishTime();

        if (!this.getLocation().equals(task.getLocation())) {
            Duration travelTime = this.getLocation().getDistanceTo(task.getLocation());
            beginTime1 = beginTime1.minus(travelTime);
            beginTime2 = beginTime2.minus(travelTime);
        }

        return (beginTime1.isBefore(endTime2) && endTime1.isAfter(beginTime2)) ||
                (beginTime2.isBefore(endTime1) && endTime2.isAfter(beginTime1));
    }
}
