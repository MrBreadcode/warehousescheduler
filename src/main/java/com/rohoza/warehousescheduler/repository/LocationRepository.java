package com.rohoza.warehousescheduler.repository;

import com.rohoza.warehousescheduler.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
