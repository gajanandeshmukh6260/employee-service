package com.happiest.assignment.es.service;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happiest.assignment.es.awsconfig.AWSConfig;
import com.happiest.assignment.es.dao.EmployeeRepository;
import com.happiest.assignment.es.dao.EventTrackingRepository;
import com.happiest.assignment.es.entity.Address;
import com.happiest.assignment.es.entity.Department;
import com.happiest.assignment.es.entity.Employee;
import com.happiest.assignment.es.entity.EventTracking;
import com.happiest.assignment.es.util.Constants;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.Silent.class)
public class EmployeeServiceTest {

    @InjectMocks
    EmployeeServiceImpl employeeService;

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    private AWSConfig aWSConfig;

    @Mock
    AmazonSQSClient amazonSQSClient;

    @Mock
    AmazonSNSClient amazonSNSClient;

    @Mock
    EventTrackingRepository eventTrackingRepository;

    @Mock
    SubscribeResult subscribeResult;

    @Mock
    CreateQueueResult createQueueResult;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateEmployee() {

        List<Department> listOfDepartments = getListOfDepartments();
        List<Address> listAddress = getListAddress();
        Employee employee = getEmployee();

        assertEquals(1, employee.getId());
        assertEquals("Abc", employee.getEmployeeName());
        assertEquals("9876543210", employee.getMobileNumber());
        assertEquals("abc@gmail.com", employee.getMailId());
        assertEquals(listAddress.size(), employee.getAddress().size());
        assertEquals(listOfDepartments.size(), employee.getDepartment().size());

        EventTracking eventTracking=new EventTracking();
        eventTracking.setId(1);
        eventTracking.setEmployeeId(1);
        eventTracking.setCreatedBy("ABC");
        eventTracking.setCreatedDate(new Date());
        eventTracking.setSnsMessageId("1234");
        eventTracking.setSqsMessageId("1234");

        assertEquals(1, eventTracking.getId());
        assertEquals(1, eventTracking.getEmployeeId());
        assertEquals("ABC", eventTracking.getCreatedBy());
        assertEquals(new Date(), eventTracking.getCreatedDate());
        assertEquals("1234", eventTracking.getSnsMessageId());
        assertEquals("1234", eventTracking.getSqsMessageId());

        CreateQueueRequest createQueueRequest = new CreateQueueRequest(Constants.EMPLOYEE_SERVICE_QUEUE_NAME);

        when(aWSConfig.getSQSClient()).thenReturn(amazonSQSClient);
        when(createQueueResult.getQueueUrl()).thenReturn("test");
        when(amazonSQSClient.createQueue(createQueueRequest)).thenReturn(createQueueResult);

        SendMessageResult sendMessageResult = new SendMessageResult();
        sendMessageResult.setMessageId("1234");

        when(amazonSQSClient.sendMessage(any(SendMessageRequest.class))).thenReturn(sendMessageResult);
        when(aWSConfig.getSnsClient()).thenReturn(amazonSNSClient);

        PublishResult publishResult=new PublishResult();
        publishResult.setMessageId("1234");

        when(amazonSNSClient.publish(any(PublishRequest.class))).thenReturn(publishResult);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(eventTrackingRepository.save(any(EventTracking.class))).thenReturn(eventTracking);
        employeeService.createEmployee(employee);

        assertEquals(1, employee.getId());
        assertEquals(1, eventTracking.getId());
    }

    @Test
    public void testCreateSNSAndPublishMsg() {
        Employee empObj = getEmployee();
        when(aWSConfig.getSnsClient()).thenReturn(mock(AmazonSNSClient.class));
        when(aWSConfig.getSnsClient().subscribe(any(SubscribeRequest.class))).thenReturn(subscribeResult);
        when(aWSConfig.getSnsClient().publish(any(PublishRequest.class))).thenReturn(mock(PublishResult.class));
        PublishResult result = employeeService.createSNSAndPublishMsg(empObj);

        assertNotNull(result);
        verify(aWSConfig.getSnsClient(), times(1)).subscribe(any(SubscribeRequest.class));
        verify(aWSConfig.getSnsClient(), times(1)).publish(any(PublishRequest.class));

    }

    @Test
    public void testConvertObjectToJsonString() throws JsonProcessingException {
        Employee empObj = getEmployee();
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(empObj)).thenReturn("jsonString");
        String jsonString = employeeService.convertObjectToJsonString(empObj);
        assertNotNull(jsonString);
    }

    @Test
    public void testCreateSQSAndSendMsg() {
        Employee empObj = new Employee();
        empObj.setId(1);
        empObj.setEmployeeName("ABC");
        empObj.setMailId("abc");

        when(aWSConfig.getSQSClient()).thenReturn(mock(AmazonSQSClient.class));
        when(aWSConfig.getSQSClient().createQueue(any(CreateQueueRequest.class)))
                .thenReturn(createQueueResult);

        SendMessageResult sendMessageResult=mock(SendMessageResult.class);
        when(aWSConfig.getSQSClient().sendMessage(any(SendMessageRequest.class)))
                .thenReturn(sendMessageResult);
        SendMessageResult result = employeeService.createSQSAndSendMsg(empObj);

        assertNotNull(result);

        verify(aWSConfig.getSQSClient(), times(1)).createQueue(any(CreateQueueRequest.class));
        verify(aWSConfig.getSQSClient(), times(1)).sendMessage(any(SendMessageRequest.class));

    }

    public List<Address> getListAddress() {
        List<Address> lstAddress = new ArrayList<>();
        lstAddress.add(new Address(1, "SB Road"));
        lstAddress.add(new Address(2, "FC Road"));
        return lstAddress;
    }

    public List<Department> getListOfDepartments() {
        List<Department> departments = new ArrayList<>();
        departments.add(new Department(1, "Development"));
        departments.add(new Department(2, "Testing"));
        return departments;
    }

    public Employee getEmployee() {
        List<Department> listOfDepartments = getListOfDepartments();
        List<Address> listAddress = getListAddress();

        Employee employee = new Employee();
        employee.setId(1);
        employee.setEmployeeName("Abc");
        employee.setMobileNumber("9876543210");
        employee.setMailId("abc@gmail.com");
        employee.setAddress(listAddress);
        employee.setDepartment(listOfDepartments);
        return employee;
    }

    @Test
    public void TestGetAllEmployee() {

        List<Employee> expectedEmployees = setListOfEmployee();
        when(employeeRepository.findAll()).thenReturn(expectedEmployees);
        List<Employee> actualEmployees = employeeService.getAllEmployee();
        verify(employeeRepository, times(1)).findAll();
        assertEquals(expectedEmployees.size(), actualEmployees.size());
        for (int i = 0; i < expectedEmployees.size(); i++) {
            assertEquals(expectedEmployees.get(i).getId(), actualEmployees.get(i).getId());
            assertEquals(expectedEmployees.get(i).getEmployeeName(), actualEmployees.get(i).getEmployeeName());
            assertEquals(expectedEmployees.get(i).getMobileNumber(), actualEmployees.get(i).getMobileNumber());
            assertEquals(expectedEmployees.get(i).getMailId(), actualEmployees.get(i).getMailId());
        }
    }

    private List<Employee> setListOfEmployee() {
        List<Department> listOfDepartments = getListOfDepartments();
        List<Address> listAddress = getListAddress();
        Employee employee1 = new Employee();
        employee1.setId(1);
        employee1.setEmployeeName("Abc");
        employee1.setMobileNumber("9876543210");
        employee1.setMailId("abc@gmail.com");
        employee1.setAddress(listAddress);
        employee1.setDepartment(listOfDepartments);

        Employee employee2 = new Employee();
        employee2.setId(2);
        employee2.setEmployeeName("xyz");
        employee2.setMobileNumber("5555555555");
        employee2.setMailId("abc@gmail.com");
        employee1.setAddress(listAddress);
        employee1.setDepartment(listOfDepartments);

        List<Employee> expectedEmployees = new ArrayList<>();
        expectedEmployees.add(employee1);
        expectedEmployees.add(employee2);
        return expectedEmployees;
    }

    @Test
    public void testGetEmployeeById() {
        Employee expectedEmployee = getEmployee();
        when(employeeRepository.findById(1)).thenReturn(Optional.of(expectedEmployee));
        Optional<Employee> actualEmployee = employeeService.getEmployeeById(1);
        verify(employeeRepository, times(1)).findById(1);
        assertTrue(actualEmployee.isPresent());
        assertEquals(expectedEmployee, actualEmployee.get());
    }

    @Test
    public void testDeleteEmployeeById() {
        Integer employeeId = 1;
        employeeService.deleteEmployeeById(employeeId);
        verify(employeeRepository, times(1)).deleteById(employeeId);
    }

    @Test
    public void testUpdateEmployee() {
        List<Address> listAddress = getListAddress();
        List<Department> listOfDepartments = getListOfDepartments();
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmployeeName("Updated Name");
        updatedEmployee.setMailId("updated@example.com");
        updatedEmployee.setMobileNumber("9876543210");
        updatedEmployee.setAddress(listAddress);
        updatedEmployee.setDepartment(listOfDepartments);

        Integer employeeId = 1;

        Employee existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setEmployeeName("Original Name");
        existingEmployee.setMailId("original@example.com");
        existingEmployee.setMobileNumber("1234567890");
        existingEmployee.setAddress(listAddress);
        existingEmployee.setDepartment(listOfDepartments);

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(updatedEmployee);

        CreateQueueRequest createQueueRequest = new CreateQueueRequest(Constants.EMPLOYEE_SERVICE_QUEUE_NAME);

        when(aWSConfig.getSQSClient()).thenReturn(amazonSQSClient);
        when(createQueueResult.getQueueUrl()).thenReturn("test");
        when(amazonSQSClient.createQueue(createQueueRequest)).thenReturn(createQueueResult);

        SendMessageResult sendMessageResult = new SendMessageResult();
        sendMessageResult.setMessageId("1234");

        when(amazonSQSClient.sendMessage(any(SendMessageRequest.class))).thenReturn(sendMessageResult);
        when(aWSConfig.getSnsClient()).thenReturn(amazonSNSClient);

        PublishResult publishResult=new PublishResult();
        publishResult.setMessageId("1234");

        when(amazonSNSClient.publish(any(PublishRequest.class))).thenReturn(publishResult);

        EventTracking eventTracking=new EventTracking();
        eventTracking.setId(1);
        eventTracking.setEmployeeId(1);
        eventTracking.setModifiedBy("ABC");
        eventTracking.setSnsMessageId("1234");
        eventTracking.setSqsMessageId("1234");

        when(eventTrackingRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(eventTracking));

        assertEquals(1, eventTracking.getId());
        assertEquals(1, eventTracking.getEmployeeId());
        assertEquals("ABC", eventTracking.getModifiedBy());
        assertEquals("1234", eventTracking.getSnsMessageId());
        assertEquals("1234", eventTracking.getSqsMessageId());

        when(eventTrackingRepository.save(any(EventTracking.class))).thenReturn(eventTracking);

       Employee result = employeeService.updateEmployee(updatedEmployee, employeeId);

        verify(employeeRepository, times(1)).findById(employeeId);
        verify(employeeRepository, times(1)).save(existingEmployee);
        assertEquals(updatedEmployee, result);
    }

    @Test
    public void testCheckMobileNoExistWhenMobileNumberExists() {
        String existingMobileNumber = "1234567890";
        when(employeeRepository.existsBymobileNumber(existingMobileNumber)).thenReturn(true);
        boolean result = employeeService.checkMobileNoExist(existingMobileNumber);
        assertTrue(result);
        verify(employeeRepository, times(1)).existsBymobileNumber(existingMobileNumber);

    }

    @Test
    public void testCheckMobileNoExistWhenMobileNumberDoesNotExist() {

        String nonExistingMobileNumber = "9876543210";
        when(employeeRepository.existsBymobileNumber(nonExistingMobileNumber)).thenReturn(false);

        boolean result = employeeService.checkMobileNoExist(nonExistingMobileNumber);

        assertFalse(result);
        verify(employeeRepository, times(1)).existsBymobileNumber(nonExistingMobileNumber);
    }

    @Test
    public void testCheckMailIdExistWhenMailIdExists() {
        String existingMailId = "abc@gmail.com";
        when(employeeRepository.existsBymailId(existingMailId)).thenReturn(true);
        boolean result = employeeService.checkMailIdExist(existingMailId);
        assertTrue(result);
        verify(employeeRepository, times(1)).existsBymailId(existingMailId);

    }

    @Test
    public void testCheckMailIdExistWhenMailIdDoesNotExist() {
        String NonExistingMailId = "abc@gmail.com";
        when(employeeRepository.existsBymailId(NonExistingMailId)).thenReturn(false);
        boolean result = employeeService.checkMailIdExist(NonExistingMailId);
        assertFalse(result);
        verify(employeeRepository, times(1)).existsBymailId(NonExistingMailId);
    }

    @Test
    public void testGetCountOfEmployee() {
        long expectedCount = 10;
        when(employeeRepository.count()).thenReturn(expectedCount);
        long actualCount = employeeService.getCountOfEmployee();

        assertEquals(expectedCount, actualCount);
        verify(employeeRepository, times(1)).count();

    }
}
