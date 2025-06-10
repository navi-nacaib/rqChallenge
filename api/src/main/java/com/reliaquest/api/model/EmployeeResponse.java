package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EmployeeResponse {

    @JsonProperty("data")
    private List<Employee> employeeList;

    @JsonProperty("status")
    private String status;

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
