package com.rohoza.warehousescheduler.service;

import com.rohoza.warehousescheduler.model.Preferences;
import com.rohoza.warehousescheduler.model.Task;
import com.rohoza.warehousescheduler.model.Worker;
import com.rohoza.warehousescheduler.repository.LocationRepository;
import com.rohoza.warehousescheduler.repository.TaskRepository;
import com.rohoza.warehousescheduler.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final TaskRepository taskRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public WorkerService(WorkerRepository workerRepository, TaskRepository taskRepository, LocationRepository locationRepository, PasswordEncoder passwordEncoder) {
        this.workerRepository = workerRepository;
        this.taskRepository = taskRepository;
        this.locationRepository = locationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Worker> findAll() {
        return workerRepository.findAll();
    }

    public List<Worker> findAllSorted() {
        return workerRepository.findAll().stream()
                .sorted(Comparator.comparing(Worker::getFirstName)
                .thenComparing(Worker::getLastName)).toList();
    }

    public Worker findById(Long id) {
        return workerRepository.findById(id).orElse(null);
    }

    public Worker findByUsername(String username) {
        return workerRepository.findByUsername(username);
    }

    public List<Worker> findAllById(List<Long> ids) {
        return workerRepository.findAllById(ids);
    }

    public Worker save(Worker worker) {
        if (worker.getId() != null) {
            Worker existingWorker = findById(worker.getId());
            if (!worker.getPassword().equals("")) {
                worker.setPassword(passwordEncoder.encode(worker.getPassword()));
            } else {
                worker.setPassword(existingWorker.getPassword());
            }
            worker.setPreferences(existingWorker.getPreferences());
        } else {
            worker.setPassword(passwordEncoder.encode(worker.getPassword()));
            Preferences preferences = new Preferences();
            preferences.setDefault();
            preferences.setWorkingLocations(locationRepository.findAll());
            worker.setPreferences(preferences);
        }
        return workerRepository.save(worker);
    }

    public Worker savePreferences(Worker worker) {
        List<Task> oldTasks = new ArrayList<>();
        for (Task task : worker.getTasks()) {
            if (task.getFinishTime().isAfter(LocalDateTime.now()) &&
                    !worker.canWorkOnTaskIgnoringAll(task)) {
                if (task.getAssignedWorkers().contains(worker)) {
                    task.getAssignedWorkers().remove(worker);
                    task.unAssign();
                    taskRepository.save(task);
                }
            }
            else {
                oldTasks.add(task);
            }
        }
        worker.getPreferences().correct();
        worker.setTasks(oldTasks);
        return workerRepository.save(worker);
    }

    public void deleteById(Long id) {
        Worker worker = findById(id);
        if (worker != null) {
            // Remove worker from all assigned tasks
            for (Task task : worker.getTasks()) {
                if (task.getAssignedWorkers().contains(worker)) {
                    task.getAssignedWorkers().remove(worker);
                    if (task.getFinishTime().isAfter(LocalDateTime.now())) {
                        task.unAssign();
                    }
                    taskRepository.save(task);
                }
            }
            // Remove worker from all declined tasks
            List<Task> tasks = taskRepository.findAll();
            for (Task task : tasks) {
                if (task.getDeclinedWorkers().contains(worker)) {
                    task.getDeclinedWorkers().remove(worker);
                    taskRepository.save(task);
                }
            }
            workerRepository.deleteById(id);
        }
    }

    public List<Worker> findAvailableWorkersForTask(Task task) {
        return workerRepository.findAll().stream()
                .filter(worker -> worker.canWorkOnTask(task))
                .collect(Collectors.toList());
    }
}
