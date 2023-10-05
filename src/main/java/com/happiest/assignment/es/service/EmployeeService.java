package com.happiest.assignment.es.service;

import com.happiest.assignment.es.entity.Employee;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface EmployeeService {

    List<Employee> getAllEmployee();

    void createEmployee(Employee employee);

    Optional<Employee> getEmployeeById(Integer id);

    void deleteEmployeeById(Integer id);

    Employee updateEmployee(Employee employee, Integer employeeId);

    boolean checkMobileNoExist(String mobileNo);

    boolean checkMailIdExist(String mailId);

    long getCountOfEmployee();
}
