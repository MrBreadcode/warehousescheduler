package com.rohoza.warehousescheduler.repository;

import com.rohoza.warehousescheduler.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
