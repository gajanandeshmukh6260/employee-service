package com.happiest.assignment.es.dao;

import com.happiest.assignment.es.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    boolean existsBymailId(String mailId);
    boolean existsBymobileNumber(String mobileNumber);
}
