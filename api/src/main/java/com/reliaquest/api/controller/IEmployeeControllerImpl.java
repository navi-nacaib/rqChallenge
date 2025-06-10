package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class IEmployeeControllerImpl implements IEmployeeController<Employee, Employee> {

    private final EmployeeService employeeService;

    private static final Logger log = LoggerFactory.getLogger(IEmployeeControllerImpl.class);

    public IEmployeeControllerImpl(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employeeList = employeeService.getAllEmployees();
        log.trace("Number of employees returned: {}", employeeList.size());

        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        return ResponseEntity.ok(employeeService.searchByName(searchString));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        return employeeService.searchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping("/highestSalary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(employeeService.getHighestSalary());
    }

    @Override
    @GetMapping("/topTenHighestEarningEmployeeNames")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(employeeService.getTopTenHighestEarningEmployeeNames());
    }

    @Override
    @PostMapping()
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employeeInput) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(employeeInput));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        employeeService.deleteEmployee(id);

        return ResponseEntity.ok("Deleted " + id);
    }
}
