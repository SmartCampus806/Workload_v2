package com.main.workload.services;

import com.main.workload.entities.AcademicLoad;
import com.main.workload.entities.StudentsGroup;
import com.main.workload.exceptions.FileParsingException;
import com.main.workload.repositories.AcademicLoadRepository;
import com.main.workload.repositories.StudentsGroupRepository;
import com.main.workload.utils.Pair;
import lombok.NonNull;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class WorkloadParserService {
    private final Set<String> words = Set.of(
            "очная форма обучения", "Базовое высшее образование", "Осенний семестр", "Весенний семестр",
            "Специализированное высшее образование", "очно-заочная форма обучения", "Бакалавриат",
            "Специалитет", "Магистратура", "Итого по кафедре", "Факультет"
    );

    public List<AcademicLoad> parse(@NonNull InputStream inputStream) {
        List<AcademicLoad> academicLoads = new ArrayList<>();

        try (Workbook workbook = new HSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            String subject = "";
            for (int rowIndex = 8; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Cell mainCell = row.getCell(0);
                    if (mainCell != null && !isCellEmpty(mainCell) && !containsAnyWord(getStringValue(0, row))) {
                        String cellValue = getStringValue(0, row);
                        if (cellValue == null)
                            continue;

                        if (!isGroup(cellValue))
                            subject = cellValue.strip();
                        else
                            academicLoads.add(extractLoad(row, subject));
                    }
                }
            }
        } catch (Exception e) {
            throw new FileParsingException("Ошибка при чтении Excel-файла, файл некоректен", e);
        }

        return academicLoads;
    }

    private AcademicLoad extractLoad(Row row, String subject) {
        return AcademicLoad.builder()
                .subject(subject)
                .groupName(Objects.requireNonNull(getStringValue(0, row)).strip())
                .course(getNumericValue(8, row))
                .semester(getNumericValue(9, row))
                .weeks(getNumericValue(10, row))
                .stream(getStringValue(11, row))
//                .additionalStream(getStringValue(13, row))
                .students(getNumericValue(14, row))
                .lecturesPlan(getNumericValue(16, row))
                .lecturesLoad(getNumericValue(18, row))
                .practicalsGroup(getNumericValue(20, row))
                .practicalsLoad(getNumericValue(21, row))
                .labsGroup(getNumericValue(23, row))
                .labsLoad(getNumericValue(24, row))
                .courseWork(getNumericValue(26, row))
                .courseProject(getNumericValue(28, row))
                .ksr(getNumericValue(30, row))
                .consult(getNumericValue(32, row))
                .rating(getNumericValue(34, row))
                .credit(getNumericValue(36, row))
                .exam(getNumericValue(38, row))
                .srs(getNumericValue(40, row))
                .practice(getNumericValue(42, row))
                .diploma(getNumericValue(44, row))
                .other(getNumericValue(46, row))
                .total(getNumericValue(48, row))
                .build();
    }

    private Integer getNumericValue(@NonNull Integer cell, @NonNull Row row) {
        return isCellEmpty(row.getCell(cell)) ? null : (int)row.getCell(cell).getNumericCellValue();
    }

    private String getStringValue(@NonNull Integer cell, @NonNull Row row) {
        return isCellEmpty(row.getCell(cell)) ? null : row.getCell(cell).getStringCellValue();
    }

    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK ||
                (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty());
    }

    private boolean isGroup(@NonNull String input) {
        String regex = "^[A-Za-zА-Яа-я]\\d{1,2}[A-Za-zА-Яа-я]-\\d{3}.*$";
        return input.matches(regex);
    }
    private boolean containsAnyWord(String text) {
        if (text != null) {
            return words.stream().anyMatch(text::contains);
        }
        return false;
    }
}