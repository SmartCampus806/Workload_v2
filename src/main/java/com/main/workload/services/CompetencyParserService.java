package com.main.workload.services;

import com.main.workload.entities.*;
import com.main.workload.repositories.EmployeeRepository;
import com.main.workload.repositories.LessonRepository;

import com.main.workload.utils.NameFormatter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import java.io.InputStream;

import com.main.workload.exceptions.FileParsingException;
import org.apache.poi.ss.usermodel.*;
import java.util.*;

@Slf4j
@Service
public class CompetencyParserService {
    private final EmployeeRepository employeeRepository;
    private final LessonRepository lessonRepository;

    public CompetencyParserService(EmployeeRepository employeeRepository, LessonRepository lessonRepository) {
        this.employeeRepository = employeeRepository;
        this.lessonRepository = lessonRepository;
    }


    public void parse(@NonNull InputStream inputStream) {

        Map<String, Employee> employees = new HashMap<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    processCompetencyRow(row);
                }
            }
        } catch (Exception e) {
            log.error("{}\n{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            throw new FileParsingException("Ошибка при чтении Excel-файла, файл некоректен", e);
        }
    }

    private void processCompetencyRow(Row row) {
        String employeeName = getStringValue(0, row);
        if (employeeName == null)
            return;
        Optional<Employee> employee = employeeRepository.findByName(NameFormatter.formatFullName(employeeName));

        if (employee.isEmpty()) {
            log.warn("Нозможно найти преподавтеля в базе по имени {}", employeeName);
            return;
        }

        String lessons = getStringValue(1, row);
        Employee employeeValue = employee.get();
        if (lessons == null)
            return;
        for (String lessonName : lessons.split(",")) {
            List<Lesson> lesson = lessonRepository.findByName(lessonName.strip());
            if (lesson.isEmpty()) {
                log.warn("Невозможно найти предмет с названием {}", lessonName);
                continue;
            }

            lesson.forEach(x-> x.addEmployee(employeeValue));
            employeeValue.addLessons(lesson);
            lessonRepository.saveAll(lesson);
        }
        employeeRepository.save(employee.get());
    }

    private String getStringValue(@NonNull Integer cell, @NonNull Row row) {
        Cell currentCell = row.getCell(cell);
        if (currentCell == null || currentCell.getCellType() == CellType.BLANK || currentCell.getCellType() == CellType.ERROR)
            return null;

        return isCellEmpty(currentCell) ? null : currentCell.getStringCellValue();
    }

    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK ||
                (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty());
    }

}
