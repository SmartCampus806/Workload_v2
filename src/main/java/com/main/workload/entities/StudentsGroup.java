package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "students_group",
        indexes = {
                @Index(name = "idx_students_group_name", columnList = "name")
        })
@Data
@NoArgsConstructor
public class StudentsGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private Integer studentsCount;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Workload> workloads;

    public StudentsGroup(String name, Integer students_count) {
        this.name = name;
        this.studentsCount = students_count;
    }
}
