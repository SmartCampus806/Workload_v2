package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WorkloadContainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Workload> workloads;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private EmployeePosition position;

    public Integer getWorkloadHours() {
        var lectureWorkload = workloads.stream()
                .filter(x -> x.getType().equals(Workload.WorkloadType.LECTURE))
                .max(Comparator.comparingInt(Workload::getWorkload))
                .map(Workload::getWorkload)
                .orElse(0);
        int otherWorkload = workloads.stream()
                .filter(x -> !x.getType().equals(Workload.WorkloadType.LECTURE))
                .mapToInt(Workload::getWorkload)
                .sum();

        return lectureWorkload + otherWorkload;
    }

    public void addWorkload(@NonNull Workload workload) {
        workloads.add(workload);
        workload.setContainer(this);
    }
    public WorkloadContainer(Lesson lesson) {
        this.lesson = lesson;
        this.workloads = new ArrayList<>();
    }
}
