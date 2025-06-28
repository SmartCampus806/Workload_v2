package com.main.workload.services;

import com.main.workload.entities.*;
import com.main.workload.repositories.EmployeePositionRepository;
import com.main.workload.repositories.EmployeeRepository;
import com.main.workload.utils.NameFormatter;
import jakarta.transaction.Transactional;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
public class EmployeeParserService {
    private final EmployeeRepository employeeRepository;
    private final EmployeePositionRepository employeePositionRepository;

    public EmployeeParserService(EmployeeRepository employeeRepository, EmployeePositionRepository employeePositionRepository) {
        this.employeeRepository = employeeRepository;
        this.employeePositionRepository = employeePositionRepository;
    }


    public void parse(@NonNull InputStream inputStream) {

        Map<String, Employee> employees = new HashMap<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    try {
                        extractEmployee(row, employees);
                    }
                    catch (Exception e) {
                        log.error("Ошибка обработки строки {}", rowIndex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("{}\n{}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            throw new FileParsingException("Ошибка при чтении Excel-файла, файл некоректен", e);
        }

        employeePositionRepository.deactivateAll(); // Деактивиреем все позиции, потом спаршеным вернем в true

        for (var employee : employees.values()) {
            Optional<Employee> savedEmployee = employeeRepository.findByName(employee.getName());
            if (savedEmployee.isEmpty()) {
                employeeRepository.save(employee);
            }
            else {
                syncEmployeePositions(savedEmployee.get(), employee.getPositions());
            }
        }
    }

    @Transactional
    public void syncEmployeePositions(Employee employee, List<EmployeePosition> parsedPositions) {
        List<EmployeePosition> existingPositions = employee.getPositions();

        for (EmployeePosition parsed : parsedPositions) {
            boolean matchFound = false;

            for (EmployeePosition existing : existingPositions) {
                if (existing.getRate().equals(parsed.getRate()) &&
                        existing.getPost().equals(parsed.getPost()) &&
                        existing.getStructuralDivision().equals(parsed.getStructuralDivision())) {

                    // Совпадает с деактивированной — просто активируем
                    existing.setActive(true);
                    matchFound = true;
                    break;
                }
            }

            if (!matchFound) {
                // Новая позиция — добавляем и активируем
                parsed.setEmployee(employee);
                parsed.setActive(true);
                employee.getPositions().add(parsed);
            }
        }

        employeeRepository.save(employee);
    }

    private void extractEmployee(Row row, Map<String, Employee> employees) {
        String employeeName = NameFormatter.formatFullName(Objects.requireNonNull(getStringValue(1, row)));
        String typeOfEmployment = getStringValue(3, row);

        Employee employee = null;
        if (employees.containsKey(employeeName)) {
            employee = employees.get(employeeName);
        }
        else {
            employee = new Employee(employeeName, typeOfEmployment);
            employees.put(employeeName, employee);
        }

        employee.addPosition(new EmployeePosition(getDoubleValue(2, row),
                EmployeePosition.Post.fromDisplayName(getStringValue(4, row).trim()),
                EmployeePosition.StructuralDivision.fromDisplayName(getStringValue(6, row))));
    }

    private Integer getNumericValue(@NonNull Integer cell, @NonNull Row row) {
        return isCellEmpty(row.getCell(cell)) ? null : (int)row.getCell(cell).getNumericCellValue();
    }
    private Double getDoubleValue(@NonNull Integer cell, @NonNull Row row) {
        return isCellEmpty(row.getCell(cell)) ? null : row.getCell(cell).getNumericCellValue();
    }

    private String getStringValue(@NonNull Integer cell, @NonNull Row row) {
        Cell currentCell = row.getCell(cell);
        if (currentCell == null || currentCell.getCellType() == CellType.BLANK || currentCell.getCellType() == CellType.ERROR)
            return "";

        return isCellEmpty(currentCell) ? "" : currentCell.getStringCellValue();
    }

    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK ||
                (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty());
    }

}
