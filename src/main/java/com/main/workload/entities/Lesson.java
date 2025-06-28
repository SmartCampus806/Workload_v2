package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lesson")
@Data
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column()
    private String name;

    private Integer semester;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkloadContainer> containers;

    @ManyToMany(mappedBy = "availableLessons")
    private List<Employee> qualifiedEmployees = new ArrayList<>();

    public void addEmployee(Employee employee) {
        qualifiedEmployees.add(employee);
    }

    public Lesson(String name, Integer semester) {
        this.name = name;
        this.semester = semester;
    }
}
