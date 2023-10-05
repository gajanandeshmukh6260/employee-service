package com.happiest.assignment.es.service;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.happiest.assignment.es.dao.EmployeeRepository;
import com.happiest.assignment.es.dao.EventTrackingRepository;
import com.happiest.assignment.es.entity.Employee;
import com.happiest.assignment.es.util.Constants;
import com.happiest.assignment.es.awsconfig.AWSConfig;
import com.happiest.assignment.es.entity.Address;
import com.happiest.assignment.es.entity.Department;
import com.happiest.assignment.es.entity.EventTracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


public class EmployeeServiceImpl implements EmployeeService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    Employee employeeDb = null;
    Address updatedAddrs = null;
    List<Address> updatedAddrLst = null;
    Department updatedDeprt = null;
    List<Department> updatedDeptLst = null;
    EventTracking eventTracking = null;


    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    AWSConfig aWSConfig;

    @Autowired
    EventTrackingRepository eventTrackingRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, AWSConfig aWSConfig, EventTrackingRepository eventTrackingRepository) {
        this.employeeRepository = employeeRepository;
        this.aWSConfig = aWSConfig;
        this.eventTrackingRepository = eventTrackingRepository;
    }

    public EmployeeServiceImpl() {
    }

    @Override
    public List<Employee> getAllEmployee() {
        LOGGER.info("getAllEmployee service layer called");
        List<Employee> employeeLst = employeeRepository.findAll();
        LOGGER.info("getAllEmployee service layer ended");
        return employeeLst;
    }

    @Override
    @Transactional
    public void createEmployee(Employee employee) {
        LOGGER.info("createEmployee service layer called");
        eventTracking = new EventTracking();
        Employee empObj = employeeRepository.save(employee);
        SendMessageResult sendMessageResult = createSQSAndSendMsg(empObj);
        PublishResult publishResult = createSNSAndPublishMsg(empObj);

        eventTracking.setEmployeeId(empObj.getId());
        eventTracking.setCreatedBy(empObj.getEmployeeName());
        eventTracking.setCreatedDate(new Date());
        eventTracking.setSqsMessageId(sendMessageResult.getMessageId());
        eventTracking.setSnsMessageId(publishResult.getMessageId());
        eventTrackingRepository.save(eventTracking);
        LOGGER.info(" createEmployee service layer end");
    }

    public SendMessageResult createSQSAndSendMsg(Employee empObj) {
        CreateQueueResult queue = aWSConfig.getSQSClient().createQueue(new CreateQueueRequest(Constants.EMPLOYEE_SERVICE_QUEUE_NAME));
        String myQueueUrl = queue.getQueueUrl();
        String employeeJsonString = convertObjectToJsonString(empObj);
        SendMessageResult sendMessageResult = aWSConfig.getSQSClient().sendMessage(new SendMessageRequest(myQueueUrl, employeeJsonString));
        LOGGER.info("sendMessageResult :" + sendMessageResult);
        return sendMessageResult;
    }

    public String convertObjectToJsonString(Employee empObj) {
        String empJsonString;
        ObjectWriter objectMapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            empJsonString = objectMapper.writeValueAsString(empObj);
            return empJsonString;
        } catch (JsonProcessingException e) {
            LOGGER.error("Error processing JSON request: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public PublishResult createSNSAndPublishMsg(Employee empObj) {
        //Subscribe request
        AmazonSNSClient snsClient = aWSConfig.getSnsClient();
        snsClient.subscribe(new SubscribeRequest(Constants.ARN_SNS_TOPIC, Constants.SQS_PROTOCOL,
                Constants.ARN_SQS_TOPIC));

        String employeeJsonString = convertObjectToJsonString(empObj);

        // publish Request
        PublishRequest publishRequest = new PublishRequest(Constants.ARN_SNS_TOPIC, employeeJsonString);
        PublishResult publishResult = snsClient.publish(publishRequest);
        LOGGER.info("publishResult :" + publishResult);
        return publishResult;
    }

    @Override
    public Optional<Employee> getEmployeeById(Integer id) {
        LOGGER.info("getEmployeeById service layer called with ID : " + id);
        Optional<Employee> employee = employeeRepository.findById(id);
        LOGGER.info("getEmployeeById service layer called with ID : " + id);
        return employee;
    }


    @Override
    public void deleteEmployeeById(Integer id) {
        LOGGER.info("deleteEmployeeById service layer called with ID : " + id);
        employeeRepository.deleteById(id);
        LOGGER.info("deleteEmployeeById service layer ended with ID : " + id);
    }

    @Override
    public Employee updateEmployee(Employee employee, Integer employeeId) {
        LOGGER.info("updateEmployee service layer called");
        Optional<Employee> empObj = getEmployeeById(employeeId);
        if (empObj.isPresent()) {
            List<Department> updatedDepartmentList = getUpdatedDepartmentList(employee);
            List<Address> updatedAddressList = getUpdatedAddressList(employee);
            employeeDb = empObj.get();
            employeeDb.setEmployeeName(employee.getEmployeeName());
            employeeDb.setMailId(employee.getMailId());
            employeeDb.setMobileNumber(employee.getMobileNumber());
            employeeDb.setDepartment(updatedDepartmentList);
            employeeDb.setAddress(updatedAddressList);
        }
        Employee emp = employeeRepository.save(employeeDb);
        getUpdatedEventTracking(employeeId, emp);
        LOGGER.info("updateEmployee service layer Ended");
        return emp;
    }

    private void getUpdatedEventTracking(Integer employeeId, Employee emp) {
        SendMessageResult sendMessageResult = createSQSAndSendMsg(emp);
        PublishResult publishResult = createSNSAndPublishMsg(emp);
        Optional<EventTracking> eventTracking1 = eventTrackingRepository.findByEmployeeId(employeeId);
        if (eventTracking1.isPresent()) {
            eventTracking = eventTracking1.get();
            eventTracking.setEmployeeId(eventTracking1.get().getEmployeeId());
            eventTracking.setModifiedBy(emp.getEmployeeName());
            eventTracking.setModifiedDate(new Date());
            eventTracking.setSnsMessageId(publishResult.getMessageId());
            eventTracking.setSqsMessageId(sendMessageResult.getMessageId());
            eventTrackingRepository.save(eventTracking);
        }
    }

    @Override
    public boolean checkMobileNoExist(String mobileNo) {
        return employeeRepository.existsBymobileNumber(mobileNo);
    }

    @Override
    public boolean checkMailIdExist(String mailId) {
        return employeeRepository.existsBymailId(mailId);
    }

    public List<Address> getUpdatedAddressList(Employee employee) {
        updatedAddrs = new Address();
        updatedAddrLst = new ArrayList<>();
        for (Address adrs : employee.getAddress()) {
            updatedAddrs = new Address();
            updatedAddrs.setId(adrs.getId());
            updatedAddrs.setAddressDetails(adrs.getAddressDetails());
            updatedAddrLst.add(updatedAddrs);
        }
        return updatedAddrLst;
    }

    public List<Department> getUpdatedDepartmentList(Employee employee) {
        updatedDeprt = new Department();
        updatedDeptLst = new ArrayList<>();
        for (Department dept : employee.getDepartment()) {
            updatedDeprt = new Department();
            updatedDeprt.setId(dept.getId());
            updatedDeprt.setDepartmentName(dept.getDepartmentName());
            updatedDeptLst.add(updatedDeprt);
        }
        return updatedDeptLst;
    }

    @Override
    public long getCountOfEmployee() {
        return employeeRepository.count();
    }
}
