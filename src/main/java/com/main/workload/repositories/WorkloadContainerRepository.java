package com.main.workload.repositories;

import com.main.workload.entities.AcademicLoad;
import com.main.workload.entities.WorkloadContainer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkloadContainerRepository extends JpaRepository<WorkloadContainer, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM WorkloadContainer wc WHERE wc.id NOT IN (SELECT DISTINCT w.container.id FROM Workload w WHERE w.container IS NOT NULL)")
    void deleteUnusedContainers();

    @Modifying
    @Query("UPDATE WorkloadContainer wc SET wc.position = null")
    void setAllPositionsToNull();

}
