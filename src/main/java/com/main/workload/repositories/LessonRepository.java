package com.main.workload.repositories;

import com.main.workload.entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Optional<Lesson> findByNameAndSemester(String name, Integer semester);
    List<Lesson> findByName(String name);

}
