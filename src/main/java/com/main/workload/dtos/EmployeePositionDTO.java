package com.main.workload.dtos;

import lombok.Data;

@Data
public class EmployeePositionDTO {
    private Long id;
    private String employeeName;
    private String post;
    private Double rate;
    private String structuralDivision;
    private Boolean active;
}