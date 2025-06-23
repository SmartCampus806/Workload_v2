package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    private String typeOfEmployment; // Вид занятости


    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeePosition> positions;

    public Employee(String name, String typeOfEmployment) {
        this.name = name;
        this.typeOfEmployment = typeOfEmployment;
        this.positions = new ArrayList<>();
    }

    public void addPosition(@NonNull EmployeePosition position) {
        position.setEmployee(this);
        positions.add(position);
    }
}

