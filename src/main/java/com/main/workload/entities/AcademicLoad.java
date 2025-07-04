package com.main.workload.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "academic_load")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupName;
    private String subject;
    private Integer course;
    private Integer semester;
    private Integer weeks;
    private String stream;
    private String additionalStream;
    private Integer students;

    // Лекции
    @Column(name = "lectures_plan")
    private Integer lecturesPlan;

    @Column(name = "lectures_load")
    private Integer lecturesLoad;

    // Практика
    @Column(name = "practicals_group")
    private Integer practicalsGroup;

    @Column(name = "practicals_load")
    private Integer practicalsLoad;

    // Лабораторные
    @Column(name = "labs_group")
    private Integer labsGroup;

    @Column(name = "labs_load")
    private Integer labsLoad;

    // Курсовые
    @Column(name = "course_work")
    private Integer courseWork;

    @Column(name = "course_project")
    private Integer courseProject;

    private Integer ksr;      // Контроль самостоятельной работы
    private Integer consult;  // Консультации
    private Integer rating;   // Рейтинг
    private Integer credit;   // Зачет
    private Integer exam;     // Экзамен
    private Integer srs;      // Самостоятельная работа студентов
    private Integer practice; // Практика TODO: как обрабатываем (две практики)???
    private Integer diploma;  // Диплом
    private Integer other;    // Другое

    private Integer total;    // Всего

    @Override
    public String toString() {
        return "AcademicLoad{" +
                ", groupName='" + groupName + '\'' +
                ", subject='" + subject + '\'' +
                ", course=" + course +
                ", semester=" + semester +
                ", weeks=" + weeks +
                ", stream='" + stream + '\'' +
                ", additionalStream='" + additionalStream + '\'' +
                ", students=" + students +
                ", lecturesPlan=" + lecturesPlan +
                ", lecturesLoad=" + lecturesLoad +
                ", practicalsGroup=" + practicalsGroup +
                ", practicalsLoad=" + practicalsLoad +
                ", labsGroup=" + labsGroup +
                ", labsLoad=" + labsLoad +
                ", courseWork=" + courseWork +
                ", courseProject=" + courseProject +
                ", ksr=" + ksr +
                ", consult=" + consult +
                ", rating=" + rating +
                ", credit=" + credit +
                ", exam=" + exam +
                ", srs=" + srs +
                ", practice=" + practice +
                ", diploma=" + diploma +
                ", other=" + other +
                ", total=" + total +
                '}';
    }
}
