package com.rohoza.warehousescheduler.repository;

import com.rohoza.warehousescheduler.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
    List<Task> findByFinishTimeAfter(LocalDateTime currentTime);
    List<Task> findByFinishTimeBefore(LocalDateTime currentTime);
    List<Task> findByFinishTimeAfterAndStartTimeBetween(LocalDateTime currentTime, LocalDateTime from, LocalDateTime to);
    List<Task> findByFinishTimeBeforeAndStartTimeBetween(LocalDateTime currentTime, LocalDateTime from, LocalDateTime to);

}
