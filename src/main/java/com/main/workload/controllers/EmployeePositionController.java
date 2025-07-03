package com.main.workload.controllers;

import com.main.workload.dtos.EmployeePositionDTO;
import com.main.workload.dtos.EmployeePositionDetailDTO;
import com.main.workload.dtos.LessonDTO;
import com.main.workload.services.EmployeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee-positions")
@Tag(name = "Преподователи", description = "API для управления преподавателями и их позициями")

public class EmployeePositionController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public List<EmployeePositionDTO> getAllEmployeePositions() {
        return employeeService.getAllEmployeePositions();
    }

    @GetMapping("/{id}")
    public EmployeePositionDetailDTO getEmployeePositionDetail(@PathVariable Long id) {
        return employeeService.getEmployeePositionDetailById(id);
    }

    @GetMapping("/lessons")
    public List<LessonDTO> getAllLessons() {
        return employeeService.getAllLessons();
    }

    @PostMapping("/{id}/add-lesson")
    public void addLessonToEmployee(@PathVariable Long id, @RequestParam Long lessonId) {
        employeeService.addLessonToEmployee(id, lessonId);
    }

    @DeleteMapping("/{id}/remove-lesson/{lessonId}")
    public void removeLessonFromEmployee(@PathVariable Long id, @PathVariable Long lessonId) {
        employeeService.removeLessonFromEmployee(id, lessonId);
    }
}