package com.rohoza.warehousescheduler.scheduler;

import com.rohoza.warehousescheduler.model.Task;
import com.rohoza.warehousescheduler.model.Worker;

import java.util.*;

class ConflictGroup {

    List<Task> tasks;
    List<Worker> workers;
    List<TaskPossibility> inProcess;
    List<TaskPossibility> assigned;
    List<TaskPossibility> declined;

    ConflictGroup(List<Task> tasks, List<Worker> workers) {
        this.tasks = tasks;
        this.workers = workers;
        this.inProcess = new ArrayList<>();
        this.assigned = new ArrayList<>();
        this.declined = new ArrayList<>();
    }

    int solve() {
        prepare();
        peacefulRound();
        chooseRound();
        swapRound();
        return assigned.size();
    }

    void prepare() {
        for (Task task : tasks) {
            TaskPossibility current = new TaskPossibility(task);
            for (Worker worker : workers) {
                if (worker.canWorkOnTask(current.task)) {
                    current.possibleWorkers.add(worker);
                }
            }
            inProcess.add(current);
        }
    }

    void peacefulRound() {
        for (TaskPossibility current : inProcess) {
            for (Worker worker : current.possibleWorkers) {
                boolean notConflicting = true;
                for (TaskPossibility possibility : inProcess) {
                    if (possibility != current && possibility.conflictsWith(current)
                            && possibility.possibleWorkers.contains(worker)) {
                        notConflicting = false;
                    }
                }
                if (notConflicting) {
                    current.notConflictingWorkers.add(worker);
                }
            }
            current.notConflictingWorkers = current.notConflictingWorkers.stream()
                    .sorted(Comparator.comparingInt(w -> w.getTaskDislike(current.task))).toList();
        }
        for (TaskPossibility possibility : inProcess) {
            for (int i = 0; i < Math.min(possibility.workersNeeded(), possibility.notConflictingWorkers.size()); i++) {
                possibility.assignedWorkers.add(possibility.notConflictingWorkers.get(i));
                possibility.possibleWorkers.remove(possibility.notConflictingWorkers.get(i));
            }
        }
        removeAssigned();
        removeDeclined();
    }

    void chooseRound() {
        while (inProcess.size() > 0) {
            inProcess.sort(Comparator.comparingInt(TaskPossibility::workersLeft));
            TaskPossibility current = inProcess.get(0);
            while (true) {
                Worker optimal = current.possibleWorkers.stream().min((a, b) ->
                                declineRate(current, a) != declineRate(current, b) ?
                                        declineRate(current, a) - declineRate(current, b) :
                                        conflictRate(current, a) != conflictRate(current, b) ?
                                                conflictRate(current, a) - conflictRate(current, b) :
                                                a.getTaskDislike(current.task) - b.getTaskDislike(current.task))
                        .orElse(null);
                if (optimal == null) {
                    break;
                }
                assignWorker(current, optimal);
                removeDeclined();
                if (current.workersLeft() == 0) {
                    assigned.add(current);
                    inProcess.remove(current);
                    break;
                }
            }
        }
        save();
    }

    void swapRound() {
        for (TaskPossibility declinedCurrent : declined) {
            List<Worker> possible = new ArrayList<>(declinedCurrent.possibleWorkers);
            for (Worker worker : possible) {
                assignWorker(declinedCurrent, worker);
            }
        }
        declined.sort(Comparator.comparingInt(TaskPossibility::workersLeft));
        for (TaskPossibility declinedCurrent : declined) {
            for (TaskPossibility assignedCurrent : assigned.stream()
                    .filter(t -> t.task.conflictsWith(declinedCurrent.task)).toList()) {
                List<Worker> spareWorkers = new ArrayList<>(assignedCurrent.possibleWorkers);
                List<Worker> swapWorkers = new ArrayList<>(assignedCurrent.assignedWorkers.stream()
                        .filter(w -> w.canWorkOnTaskIgnoringOne(declinedCurrent.task, assignedCurrent.task)).toList());
                spareWorkers = spareWorkers.stream().sorted(Comparator.comparingInt(w -> w.getTaskDislike(assignedCurrent.task))).toList();
                swapWorkers = swapWorkers.stream().sorted(Comparator.comparingInt(w -> w.getTaskDislike(declinedCurrent.task))).toList();
                for (int i = 0; i < Math.min(declinedCurrent.workersLeft(),
                        Math.min(spareWorkers.size(), swapWorkers.size())); i++) {
                    assignedCurrent.possibleWorkers.remove(spareWorkers.get(i));
                    assignedCurrent.assignedWorkers.add(spareWorkers.get(i));
                    assignedCurrent.assignedWorkers.remove(swapWorkers.get(i));
                    declinedCurrent.assignedWorkers.add(swapWorkers.get(i));
                }
                if (declinedCurrent.workersLeft() == 0) {
                    break;
                }
            }
            if (declinedCurrent.workersLeft() == 0) {
                assigned.add(declinedCurrent);
            }
        }
        save();
    }

    void save() {
        for (TaskPossibility current : assigned) {
            current.assign();
        }
    }

    int declineRate(TaskPossibility current, Worker worker) {
        int declined = 0;
        for (TaskPossibility possibility : inProcess) {
            if (possibility != current && possibility.conflictsWith(current) && possibility.willDecline(worker)) {
                declined++;
            }
        }
        return declined;
    }

    int conflictRate(TaskPossibility current, Worker worker) {
        int conflict = 0;
        for (TaskPossibility possibility : inProcess) {
            if (possibility != current && possibility.conflictsWith(current) && possibility.possibleWorkers.contains(worker)) {
                conflict++;
            }
        }
        return conflict;
    }

    void assignWorker(TaskPossibility current, Worker worker) {
        current.assignedWorkers.add(worker);
        current.possibleWorkers.remove(worker);
        for (TaskPossibility possibility : inProcess) {
            if (possibility == current || possibility.conflictsWith(current)) {
                possibility.possibleWorkers.remove(worker);
            }
        }
        for (TaskPossibility possibility : assigned) {
            if (possibility.conflictsWith(current)) {
                possibility.possibleWorkers.remove(worker);
            }
        }
        for (TaskPossibility possibility : declined) {
            if (possibility.conflictsWith(current)) {
                possibility.possibleWorkers.remove(worker);
            }
        }
    }

    void removeAssigned() {
        List<TaskPossibility> inProcess = new ArrayList<>();
        for (TaskPossibility possibility : this.inProcess) {
            if (possibility.workersLeft() > 0) {
                inProcess.add(possibility);
            }
            else {
                assigned.add(possibility);
            }
        }
        this.inProcess = inProcess;
    }

    void removeDeclined() {
        List<TaskPossibility> inProcess = new ArrayList<>();
        for (TaskPossibility possibility : this.inProcess) {
            if (possibility.workersLeft() <= possibility.possibleWorkers.size()) {
                inProcess.add(possibility);
            }
            else {
                declined.add(possibility);
            }
        }
        this.inProcess = inProcess;
    }
}
