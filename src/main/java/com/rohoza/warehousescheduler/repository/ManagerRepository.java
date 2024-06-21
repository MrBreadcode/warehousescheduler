package com.rohoza.warehousescheduler.repository;

import com.rohoza.warehousescheduler.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
}
