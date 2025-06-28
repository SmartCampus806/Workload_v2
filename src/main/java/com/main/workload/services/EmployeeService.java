package com.main.workload.services;

import com.main.workload.entities.Employee;
import com.main.workload.entities.EmployeePosition;
import com.main.workload.repositories.EmployeePositionRepository;
import com.main.workload.repositories.EmployeeRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeePositionRepository employeePositionRepository;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeePositionRepository employeePositionRepository) {
        this.employeeRepository = employeeRepository;
        this.employeePositionRepository = employeePositionRepository;
    }

    public List<Employee> getEmployeesWithoutActivePositions() {
        List<Employee> res = employeeRepository.findByPositionsActiveFalse();
        res.addAll(employeeRepository.findByPositionsIsEmpty());
        return res;
    }

    public List<EmployeePosition> getPositionsWithoutCompetencesByStructuralDivision(@NonNull EmployeePosition.StructuralDivision division) {
        return employeePositionRepository.findAllByStructuralDivision(division).stream()
                .filter(x -> x.getEmployee().getAvailableLessons().isEmpty())
                .toList();
    }
}