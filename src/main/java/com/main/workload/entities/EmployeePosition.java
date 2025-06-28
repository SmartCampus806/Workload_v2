package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_position")
@Data
@NoArgsConstructor
public class EmployeePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double rate; // Ставка

    private Post post; // Должность

    @Column(name = "structural_division")
    private StructuralDivision structuralDivision; // Структурное подразделение

    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public Double getFullWorkload() {
        return rate * 830 + 300;
    }

    public EmployeePosition(Double rate, Post post, StructuralDivision structuralDivision) {
        this.rate = rate;
        this.post = post;
        this.structuralDivision = structuralDivision;
        this.active = true;
    }

    @Getter
    @AllArgsConstructor
    public enum StructuralDivision {
        DEPARTMENT_806("кафедра 806"),
        NIO_806("НИО-806"),
        INSTITUTE_8("Дирекция Института №8 \"Компьютерные науки и прикладная математика\"");

        private final String displayName;

        public static @NonNull StructuralDivision fromDisplayName(@NonNull String displayName) throws IllegalArgumentException {
            for (StructuralDivision division : StructuralDivision.values()) {
                if (division.getDisplayName().equals(displayName)) {
                    return division;
                }
            }
            throw new IllegalArgumentException("No StructuralDivision found for display name: " + displayName);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Post {
        DEPUTY_DIRECTOR("заместитель директора"),
        LEADING_ELECTRONICS_ENGINEER("ведущий электроник"),
        LEADING_MATHEMATICIAN("ведущий математик"),
        ENGINEER_CATEGORY_1("инженер 1 категории"),
        ENGINEER_CATEGORY_2("инженер 2 категории"),
        SENIOR_RESEARCHER_WITH_DEGREE("старший научный сотрудник с у/с"),
        DEPARTMENT_HEAD("зав. кафедрой"),
        ENGINEER("инженер"),
        ASSOCIATE_PROFESSOR("доцент"),
        LEADING_METHODOLOGY_SPECIALIST("ведущий специалист по учебно-методической работе"),
        TECHNICIAN("техник"),
        DIRECTOR("директор"),
        ASSISTANT("ассистент"),
        RESEARCHER_WITH_DEGREE("научный сотрудник с у/с"),
        SENIOR_LECTURER("ст. преподаватель"),
        PROFESSOR("профессор"),
        JUNIOR_RESEARCHER_WITHOUT_DEGREE("младший научный сотрудник без у/с"),
        LEADING_ENGINEER("ведущий инженер"),
        INSPECTOR("инспектор"),
        LEADING_RESEARCHER_WITH_DEGREE("ведущий научный сотрудник с у/с (д.н.)"),
        DEPUTY_DIRECTOR_FOR_EXTRACURRICULAR("заместитель директора по внеучебной работе");

        private final String displayName;

        public static @NonNull Post fromDisplayName(@NonNull String displayName) throws IllegalArgumentException {
            for (Post post : Post.values()) {
                if (post.getDisplayName().equalsIgnoreCase(displayName)) {
                    return post;
                }
            }
            throw new IllegalArgumentException("No Post found for display name: " + displayName);
        }
    }
}


