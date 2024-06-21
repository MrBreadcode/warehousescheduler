package com.rohoza.warehousescheduler.scheduler;

import com.rohoza.warehousescheduler.model.Task;
import com.rohoza.warehousescheduler.model.Worker;

import java.util.*;

public class Scheduler {

    private List<Task> tasks;
    private List<Worker> allWorkers;
    private List<ConflictGroup> conflictGroups;
    private int assigned = 0;

    public Scheduler(List<Task> tasks, List<Worker> allWorkers) {
        this.tasks = tasks;
        this.allWorkers = allWorkers;
        this.conflictGroups = new ArrayList<>();
    }

    private void prepare() {
        List<Task> workingOn = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.isManually()) {
                task.unAssign();
                workingOn.add(task);
            }
            else {
                assigned++;
            }
        }
        tasks = workingOn;
        Collections.shuffle(allWorkers);
    }

    private void buildGroups() {
        while (!tasks.isEmpty()) {
            List<Task> conflict = new ArrayList<>();
            conflict.add(tasks.get(0));
            tasks.remove(0);

            boolean areConflicts = true;

            while (areConflicts) {
                for (int i = 0; i < conflict.size(); i++) {
                    for (Task unchecked : tasks) {
                        if (conflict.get(i).conflictsWith(unchecked)) {
                            conflict.add(unchecked);
                        }
                    }
                    for (Task task : conflict) {
                        tasks.remove(task);
                    }
                }
                boolean noConflicts = true;
                for (Task task : conflict) {
                    for (Task unchecked : tasks) {
                        if (task.conflictsWith(unchecked)) {
                            noConflicts = false;
                        }
                    }
                }
                if (noConflicts) {
                    areConflicts = false;
                }
            }
            conflictGroups.add(new ConflictGroup(conflict, allWorkers));
        }
    }

    public int generate() {
        prepare();
        buildGroups();
        for (ConflictGroup group : conflictGroups) {
            assigned += group.solve();
        }
        return assigned;
    }
}
