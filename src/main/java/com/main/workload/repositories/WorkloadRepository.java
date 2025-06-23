package com.main.workload.repositories;

import com.main.workload.entities.Workload;
import com.main.workload.entities.WorkloadContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkloadRepository extends JpaRepository<Workload, Long> {

}

