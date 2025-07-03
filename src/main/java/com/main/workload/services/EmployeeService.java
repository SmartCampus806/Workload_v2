package com.main.workload.services;

import com.main.workload.dtos.EmployeePositionDTO;
import com.main.workload.dtos.EmployeePositionDetailDTO;
import com.main.workload.dtos.LessonDTO;
import com.main.workload.entities.Employee;
import com.main.workload.entities.EmployeePosition;
import com.main.workload.entities.Lesson;
import com.main.workload.repositories.EmployeePositionRepository;
import com.main.workload.repositories.EmployeeRepository;
import com.main.workload.repositories.LessonRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeePositionRepository employeePositionRepository;
    private final LessonRepository lessonRepository;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeePositionRepository employeePositionRepository, LessonRepository lessonRepository) {
        this.employeeRepository = employeeRepository;
        this.employeePositionRepository = employeePositionRepository;
        this.lessonRepository = lessonRepository;
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


    public List<EmployeePositionDTO> getAllEmployeePositions() {
        List<EmployeePosition> positions = employeePositionRepository.findAll();
        return positions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private EmployeePositionDTO mapToDTO(EmployeePosition position) {
        EmployeePositionDTO dto = new EmployeePositionDTO();
        dto.setId(position.getId());
        dto.setEmployeeName(position.getEmployee().getName());
        dto.setPost(position.getPost().getDisplayName());
        dto.setRate(position.getRate());
        dto.setStructuralDivision(position.getStructuralDivision().getDisplayName());
        dto.setActive(position.getActive());
        return dto;
    }

    public EmployeePositionDetailDTO getEmployeePositionDetailById(Long id) {
        EmployeePosition position = employeePositionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        Employee employee = position.getEmployee();
        List<Lesson> lessons = employee.getAvailableLessons();

        EmployeePositionDetailDTO dto = new EmployeePositionDetailDTO();
        dto.setId(position.getId());
        dto.setEmployeeName(employee.getName());
        dto.setPost(position.getPost().getDisplayName());
        dto.setRate(position.getRate());
        dto.setStructuralDivision(position.getStructuralDivision().getDisplayName());
        dto.setActive(position.getActive());
        dto.setCompetencies(lessons.stream().map(this::mapLessonToDTO).collect(Collectors.toList()));
        return dto;
    }

    private LessonDTO mapLessonToDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setName(lesson.getName());
        return dto;
    }

    public List<LessonDTO> getAllLessons() {
        List<Lesson> lessons = lessonRepository.findAll();
        return lessons.stream().map(this::mapLessonToDTO).collect(Collectors.toList());
    }

    public void addLessonToEmployee(Long positionId, Long lessonId) {
        EmployeePosition position = employeePositionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        Employee employee = position.getEmployee();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        if (!employee.getAvailableLessons().contains(lesson)) {
            employee.addLesson(lesson);
            employeeRepository.save(employee);
        }
    }

    public void removeLessonFromEmployee(Long positionId, Long lessonId) {
        EmployeePosition position = employeePositionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        Employee employee = position.getEmployee();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        if (employee.getAvailableLessons().contains(lesson)) {
            employee.getAvailableLessons().remove(lesson);
            employeeRepository.save(employee);
        }
    }
}