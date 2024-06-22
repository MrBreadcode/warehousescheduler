package com.rohoza.warehousescheduler.service;

import com.rohoza.warehousescheduler.model.Location;
import com.rohoza.warehousescheduler.model.Task;
import com.rohoza.warehousescheduler.model.Worker;
import com.rohoza.warehousescheduler.repository.LocationRepository;
import com.rohoza.warehousescheduler.repository.TaskRepository;
import com.rohoza.warehousescheduler.repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final TaskRepository taskRepository;
    private final WorkerRepository workerRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository,
                           TaskRepository taskRepository,
                           WorkerRepository workerRepository) {
        this.locationRepository = locationRepository;
        this.taskRepository = taskRepository;
        this.workerRepository = workerRepository;
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public List<Location> findAllOther(Long id) {
        return locationRepository.findAll().stream()
                .filter(loc -> !loc.getId().equals(id))
                .collect(Collectors.toList());
    }

    public Location findById(Long id) {
        return locationRepository.findById(id).orElse(null);
    }

    @Transactional
    public Location save(Location location) {
        Location savedLocation = locationRepository.save(location);

        // Синхронізація відстаней для інших локацій
        for (Map.Entry<Location, Duration> entry : location.getDistanceToOtherLocations().entrySet()) {
            Location otherLocation = entry.getKey();
            Duration distance = entry.getValue();

            // Зберегти зворотню відстань
            otherLocation.getDistanceToOtherLocations().put(savedLocation, distance);
            locationRepository.save(otherLocation);
        }

        return savedLocation;
    }

    @Transactional
    public void deleteById(Long id) {
        Location location = findById(id);
        if (location != null) {

            List<Location> allLocations = findAll();
            for (Location loc : allLocations) {
                if (loc.getDistanceToOtherLocations().containsKey(location)) {
                    loc.getDistanceToOtherLocations().remove(location);
                    locationRepository.save(loc);
                }
            }
            location.getDistanceToOtherLocations().clear();

            List<Task> allTasks = taskRepository.findAll();
            for (Task task : allTasks) {
                if (task.getLocation().equals(location)) {
                    taskRepository.deleteById(task.getId());
                }
            }

            List<Worker> allWorkers = workerRepository.findAll();
            for (Worker worker : allWorkers) {
                if (worker.getPreferences().getWorkingLocations().contains(location)) {
                    worker.getPreferences().getWorkingLocations().remove(location);
                    workerRepository.save(worker);
                }
            }

            locationRepository.save(location);
            locationRepository.deleteById(id);
        }
    }
}
