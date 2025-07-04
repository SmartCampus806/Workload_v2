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

    private Boolean active;

    public Workload(WorkloadType type, int workload, StudentsGroup group) {
        this.type = type;
        this.workload = workload;
        this.group = group;
        this.active = true;
    }

    @Getter
    public enum WorkloadType {
        LECTURE("Лекция"),
        PRACTICE("Практика"),
        LABORATORY_WORK("Лабораторная работа"),
        EXAM("Экзамен"),
        RATING("Рейтинг"),
        CREDIT("Зачет"),
        CONSULT("Консультация"),
        KSR("КСР"),
        DIPLOMA("Диплом"),
        OTHER("Другое"),
        COURSE_WORK("Курсовая работа"),
        COURSE_PROJECT("Курсовой проект");

        private final String translation;

        WorkloadType(String translation) {
            this.translation = translation;
        }
    }

    @Override
    public String toString() {
        return "Workload{" +
                "id=" + id +
                ", type=" + type +
                ", workload=" + workload +
                '}';
    }
}
