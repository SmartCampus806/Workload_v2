package com.main.workload.controllers;

import com.main.workload.dtos.EmployeeWithPositionsDTO;
import com.main.workload.entities.Employee;
import com.main.workload.entities.EmployeePosition;
import com.main.workload.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Валидация", description = "API для проверки валидности данных и исключения ручных ошибок")
public class CheckController {

    private final EmployeeService employeeService;

    @Autowired
    public CheckController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    private static final Set<EmployeePosition.Post> EXCLUDED_POSTS = Set.of(
            EmployeePosition.Post.ENGINEER,
            EmployeePosition.Post.ENGINEER_CATEGORY_1,
            EmployeePosition.Post.ENGINEER_CATEGORY_2,
            EmployeePosition.Post.TECHNICIAN,
            EmployeePosition.Post.LEADING_METHODOLOGY_SPECIALIST,
            EmployeePosition.Post.LEADING_ENGINEER,
            EmployeePosition.Post.LEADING_ELECTRONICS_ENGINEER
    );
    @GetMapping("/employees/check-competences")
    public List<EmployeeWithPositionsDTO> checkCompetences(@RequestParam String structuralDivision) {
        EmployeePosition.StructuralDivision division = EmployeePosition.StructuralDivision.fromDisplayName(structuralDivision);
        List<EmployeePosition> positions = employeeService.getPositionsWithoutCompetencesByStructuralDivision(division);

        return positions.stream()
                .filter(position -> !EXCLUDED_POSTS.contains(position.getPost()))
                .collect(Collectors.groupingBy(EmployeePosition::getEmployee))
                .entrySet().stream()
                .map(entry -> new EmployeeWithPositionsDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    @GetMapping("/employees/without-active-positions")
    public List<Employee> getEmployeesWithoutActivePositions() {
        return employeeService.getEmployeesWithoutActivePositions();
    }
}


