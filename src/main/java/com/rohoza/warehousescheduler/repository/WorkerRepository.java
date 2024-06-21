package com.rohoza.warehousescheduler.repository;

import com.rohoza.warehousescheduler.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Worker findByUsername(String username);
}
