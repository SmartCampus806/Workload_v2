package com.main.workload.repositories;

import com.main.workload.entities.EmployeePosition;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, Long> {
    Optional<EmployeePosition> findByIdAndStructuralDivision(Long id, String structuralDivision);
    List<EmployeePosition> findAllByStructuralDivision(EmployeePosition.StructuralDivision structuralDivision);
    List<EmployeePosition> findByActiveTrueAndStructuralDivision(EmployeePosition.StructuralDivision division);
    @Modifying
    @Transactional
    @Query("UPDATE EmployeePosition e SET e.active = false")
    void deactivateAll();
}
