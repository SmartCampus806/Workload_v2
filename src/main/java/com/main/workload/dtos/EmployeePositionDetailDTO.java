package com.main.workload.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class EmployeePositionDetailDTO {
    private Long id;
    private String employeeName;
    private String post;
    private Double rate;
    private String structuralDivision;
    private Boolean active;
    private List<LessonDTO> competencies;
}