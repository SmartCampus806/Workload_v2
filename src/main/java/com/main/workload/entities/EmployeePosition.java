package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_position")
@Data
@NoArgsConstructor
public class EmployeePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Double rate; // Ставка

    private String post; // Должность

    private String structuralDivision; // Структурное подразделение
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;


    public EmployeePosition(Double rate, String post, String structuralDivision) {
        this.rate = rate;
        this.post = post;
        this.structuralDivision = structuralDivision;
        this.active = true;
    }
}


