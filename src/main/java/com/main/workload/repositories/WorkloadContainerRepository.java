package com.main.workload.repositories;

import com.main.workload.entities.AcademicLoad;
import com.main.workload.entities.WorkloadContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkloadContainerRepository extends JpaRepository<WorkloadContainer, Long> {

}
