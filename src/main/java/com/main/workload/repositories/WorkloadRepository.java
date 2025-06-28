package com.main.workload.repositories;

import com.main.workload.entities.Lesson;
import com.main.workload.entities.StudentsGroup;
import com.main.workload.entities.Workload;
import com.main.workload.entities.WorkloadContainer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkloadRepository extends JpaRepository<Workload, Long> {
    @Query("SELECT w FROM Workload w " +
            "JOIN w.container c " +
            "JOIN c.lesson l " +
            "WHERE l = :lesson " +
            "AND w.workload = :workload " +
            "AND w.type = :type " +
            "AND w.group = :group")
    List<Workload> findByLessonAndWorkloadAndTypeAndGroup(
            @Param("lesson") Lesson lesson,
            @Param("workload") int workload,
            @Param("type") Workload.WorkloadType type,
            @Param("group") StudentsGroup group);

    @Modifying
    @Transactional
    @Query("UPDATE Workload w SET w.active = false")
    void deactivateAll();

    @Modifying
    @Transactional
    @Query("DELETE FROM Workload w WHERE w.active = false")
    void deleteAllInactive();

    @Query("SELECT w FROM Workload w WHERE w.group.name LIKE CONCAT('М', :faculty, 'О%')")
    List<Workload> findAllByGroupFaculty(@Param("faculty") String faculty);
}

