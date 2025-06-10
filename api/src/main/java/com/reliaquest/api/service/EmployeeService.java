package com.reliaquest.api.service;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.Response;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeService {

    private final RestTemplate restTemplate;
    private final String employeeApi = "http://localhost:8112/api/v1/employee";

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    public EmployeeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Employee> getAllEmployees() {

        EmployeeResponse employeeResponse = restTemplate.getForObject(employeeApi, EmployeeResponse.class);

        if (employeeResponse == null) {
            log.trace("No employees returned");
            return List.of();
        } else {
            return employeeResponse.getEmployeeList();
        }
    }

    public List<Employee> searchByName(String searchString) {

        log.trace("Searching employees that contain: '{}'", searchString);

        // Make sure to lowercase searchString
        List<Employee> employeeList = getAllEmployees().stream()
                .filter(employee -> employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                .toList();

        if (employeeList.isEmpty()) {
            log.trace("No employees found");
        } else {
            log.trace("{} matching employee(s) found", employeeList.size());
        }

        return employeeList;
    }

    public Optional<Employee> searchById(String id) {

        log.trace("Searching employee by id: {}", id);

        // Make sure to use equals when comparing strings and not ==
        Optional<Employee> employeeOptional = getAllEmployees().stream()
                .filter(employee -> employee.getId().equals(id))
                .findFirst();

        if (employeeOptional.isPresent()) {
            log.trace("Employee id {} found", id);
        } else {
            log.trace("Employee not found");
        }

        return employeeOptional;
    }

    public int getHighestSalary() {

        log.trace("Finding highest salary");

        // Find the max int given a list of Employee objects
        int highestSalary =
                getAllEmployees().stream().mapToInt(Employee::getSalary).max().orElse(0);

        log.trace("Highest salary found: {}", highestSalary);

        return highestSalary;
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {

        log.trace("Finding top 10 highest earners");

        // Comparator will use natural ordering (i.e. ascending) so we want to reverse it and get the first 10
        List<String> employeeList = getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .toList();

        log.trace("Top 10 highest earners: {}", employeeList);

        return employeeList;
    }

    public Employee createEmployee(Employee employeeInput) {

        // Mapping my Employee class to CreateMockEmployeeInput class since the server API requires this as an input
        CreateMockEmployeeInput createMockEmployeeInput = createMockEmployeeInput(employeeInput);

        // Build headers and body
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateMockEmployeeInput> requestEntity = new HttpEntity<>(createMockEmployeeInput, httpHeaders);

        // Exchange with ParameterizedTypeReference
        ResponseEntity<Response<Employee>> responseEntity = restTemplate.exchange(
                employeeApi, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {});

        return Objects.requireNonNull(responseEntity.getBody()).data();
    }

    public void deleteEmployee(String id) {

        // Mapping my id to DeleteMockEmployeeInput class since the server API requires this as an input
        DeleteMockEmployeeInput deleteMockEmployeeInput = createDeleteMockEmployeeInput(id);

        // Build headers and body
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeleteMockEmployeeInput> requestEntity = new HttpEntity<>(deleteMockEmployeeInput, httpHeaders);

        // Exchange with ParameterizedTypeReference
        restTemplate.exchange(employeeApi, HttpMethod.DELETE, requestEntity, new ParameterizedTypeReference<>() {});
    }

    private CreateMockEmployeeInput createMockEmployeeInput(Employee employee) {
        CreateMockEmployeeInput createMockEmployeeInput = new CreateMockEmployeeInput();

        createMockEmployeeInput.setName(employee.getName());
        createMockEmployeeInput.setSalary(employee.getSalary());
        createMockEmployeeInput.setAge(employee.getAge());
        createMockEmployeeInput.setTitle(employee.getTitle());

        return createMockEmployeeInput;
    }

    private DeleteMockEmployeeInput createDeleteMockEmployeeInput(String id) {
        DeleteMockEmployeeInput deleteMockEmployeeInput = new DeleteMockEmployeeInput();

        deleteMockEmployeeInput.setName(id);

        return deleteMockEmployeeInput;
    }
}
