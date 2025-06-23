package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Workload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkloadType type;

    @Column(nullable = false)
    private int workload;

    @ManyToOne
    @JoinColumn(name = "container_id")
    private WorkloadContainer container;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private StudentsGroup group;

    public Workload(WorkloadType type, int workload, StudentsGroup group) {
        this.type = type;
        this.workload = workload;
        this.group = group;
    }

    public enum WorkloadType {
        LECTURE,
        PRACTICE,
        LABORATORY_WORK,
        EXAM,
        RATING,
        CREDIT,
        CONSULT,
        KSR,
        DIPLOMA,
        OTHER,
        COURSE_WORK,
        COURSE_PROJECT
    }
}
