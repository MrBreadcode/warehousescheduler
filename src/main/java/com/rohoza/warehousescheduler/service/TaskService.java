package com.rohoza.warehousescheduler.service;

import com.rohoza.warehousescheduler.model.Task;
import com.rohoza.warehousescheduler.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public List<Task> findCurrentAll() {
        return taskRepository.findByFinishTimeAfter(LocalDateTime.now());
    }

    public List<Task> findArchiveAll() {
        return taskRepository.findByFinishTimeBefore(LocalDateTime.now());
    }

    public List<Task> findByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return taskRepository.findByStartTimeBetween(fromDate, toDate);
    }

    public List<Task> findCurrentByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return taskRepository.findByFinishTimeAfterAndStartTimeBetween(LocalDateTime.now(), fromDate, toDate);
    }

    public List<Task> findArchiveByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return taskRepository.findByFinishTimeBeforeAndStartTimeBetween(LocalDateTime.now(), fromDate, toDate);
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }
}
