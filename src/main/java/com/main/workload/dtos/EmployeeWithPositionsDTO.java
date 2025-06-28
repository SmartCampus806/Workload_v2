package com.main.workload.dtos;

import com.main.workload.entities.Employee;
import com.main.workload.entities.EmployeePosition;
import lombok.Data;

import java.util.List;

@Data
public class EmployeeWithPositionsDTO {
    private Long id;
    private String name;
    private String typeOfEmployment;
    private List<PositionDTO> positions;

    public EmployeeWithPositionsDTO(Employee employee, List<EmployeePosition> positions) {
        this.id = employee.getId();
        this.name = employee.getName();
        this.typeOfEmployment = employee.getTypeOfEmployment();
        this.positions = positions.stream().map(PositionDTO::new).toList();
    }

    @Data
    public static class PositionDTO {
        private Long id;
        private String post;
        private String structuralDivision;
        private Double rate;
        private Boolean active;

        public PositionDTO(EmployeePosition position) {
            this.id = position.getId();
            this.post = position.getPost().getDisplayName();
            this.structuralDivision = position.getStructuralDivision().getDisplayName();
            this.rate = position.getRate();
            this.active = position.getActive();
        }
    }
}

