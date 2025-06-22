package com.main.workload.repositories;

import com.main.workload.entities.AcademicLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicLoadRepository extends JpaRepository<AcademicLoad, Long> {

    // Найти записи по названию группы
    List<AcademicLoad> findByGroupName(String groupName);

    // Найти записи по предмету и курсу
    List<AcademicLoad> findBySubjectAndCourse(String subject, Integer course);

    // Найти по названию группы, предмету, курсу и семестру
    List<AcademicLoad> findByGroupNameAndSubjectAndCourseAndSemester(
            String groupName, String subject, Integer course, Integer semester
    );
}
