package com.happiest.assignment.es.dao;

import com.happiest.assignment.es.entity.EventTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventTrackingRepository extends JpaRepository<EventTracking,Integer> {
    Optional<EventTracking> findByEmployeeId(Integer employeeId);
}
