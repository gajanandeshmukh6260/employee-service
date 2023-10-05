package com.happiest.assignment.es.controller;


import com.happiest.assignment.es.entity.Employee;
import com.happiest.assignment.es.service.EmployeeService;
import com.happiest.assignment.es.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/employee-service", produces = Constants.APPLICATION_JSON)
public class EmployeeController {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    EmployeeService employeeService;

    private Employee employee;

    @GetMapping("/allEmployee")
    private List<Employee> getAllEmployee() {
        LOGGER.info("All employee fetching started");
        List<Employee> allEmployee = employeeService.getAllEmployee();
        LOGGER.info("All employee fetching ended");
        return allEmployee;
    }

    @PostMapping("/addEmployee")
    private ResponseEntity<?> addEmployee(@RequestBody Employee employee) {
        LOGGER.info("Creating employee is started");
        if (employeeService.checkMailIdExist(employee.getMailId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists!");
        } else if (employeeService.checkMobileNoExist(employee.getMobileNumber())) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile Number already exists!");
        } else {
            employeeService.createEmployee(employee);
            LOGGER.info("Employee created successfully");
            return ResponseEntity.ok("Employee Created successFully...!");
        }
    }

    @GetMapping("/employee/{id}")
    private Employee getEmployeeById(@PathVariable("id") Integer id) {

        LOGGER.info("Fetching employee started with ID:" + id);
        Optional<Employee> employeeById = employeeService.getEmployeeById(id);
        if (employeeById.isPresent()) {
            employee = employeeById.get();
        }
        LOGGER.info("Fetching employee ended with ID:" + id);
        return employee;
    }

    @DeleteMapping("/delete-employee/{id}")
    private void deleteEmployeeById(@PathVariable("id") Integer id) {
        LOGGER.info("Deleting employee started with ID:" + id);
        employeeService.deleteEmployeeById(id);
        LOGGER.info("Deleting employee ended with ID:" + id);
    }

    @PutMapping("/update-employee/{id}")
    private Employee updateEmployee(@RequestBody Employee employee, @PathVariable("id") Integer employeeId) {
        LOGGER.info("Update Employee started with Id:" + employeeId);
        Employee updatedEmployee = employeeService.updateEmployee(employee, employeeId);
        LOGGER.info("Update Employee End with Id:" + employeeId);
        return updatedEmployee;
    }

    @GetMapping("/employee/count")
    public long getCountOfEmployee() {
        LOGGER.info("Count of Employee started :" );
        long countOfEmployee = employeeService.getCountOfEmployee();
        LOGGER.info("Count of Employee started :" + countOfEmployee);
        return countOfEmployee;
    }
}