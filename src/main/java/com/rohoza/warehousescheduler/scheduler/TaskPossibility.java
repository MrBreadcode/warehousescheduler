package com.rohoza.warehousescheduler.scheduler;

import com.rohoza.warehousescheduler.model.Task;
import com.rohoza.warehousescheduler.model.Worker;

import java.util.ArrayList;
import java.util.List;

class TaskPossibility {

    Task task;
    List<Worker> assignedWorkers = new ArrayList<>();
    List<Worker> notConflictingWorkers = new ArrayList<>();
    List<Worker> possibleWorkers = new ArrayList<>();

    TaskPossibility(Task task) {
        this.task = task;
    }

    int workersNeeded() {
        return task.getNeededWorkers();
    }

    int workersLeft() {
        return workersNeeded() - assignedWorkers.size();
    }

    void assign() {
        task.assignWorkers(assignedWorkers);
    }

    boolean willDecline(Worker worker) {
        if (!possibleWorkers.contains(worker))
            return false;
        return possibleWorkers.size() - 1 < workersNeeded();
    }

    boolean conflictsWith(TaskPossibility another) {
        return this.task.conflictsWith(another.task);
    }
}
