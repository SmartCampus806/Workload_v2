package com.main.workload.repositories;

import com.main.workload.entities.StudentsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface StudentsGroupRepository extends JpaRepository<StudentsGroup, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO students_group(name, students_count)
        VALUES (:#{#group.name}, :#{#group.students_count})
        ON CONFLICT (name)
        DO UPDATE SET students_count = EXCLUDED.students_count
        """, nativeQuery = true)
    void upsertByName(@Param("group") StudentsGroup group);
}
