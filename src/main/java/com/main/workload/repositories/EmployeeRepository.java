package com.main.workload.repositories;

import com.main.workload.entities.Employee;
import com.main.workload.entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByName(String name);

    List<Employee> findByPositionsIsEmpty();
    List<Employee> findByPositionsActiveFalse();
}
