package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table( name = "students_group",
        indexes = {
            @Index(name = "idx_students_group_name", columnList = "name")
        })
@Data
public class StudentsGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
    private Integer students_count;

    public StudentsGroup(String name, Integer students_count) {
        this.name = name;
        this.students_count = students_count;
    }
}


