package com.main.workload.services;

import com.main.workload.entities.StudentsGroup;
import com.main.workload.entities.Workload;
import com.main.workload.entities.WorkloadContainer;
import com.main.workload.repositories.WorkloadContainerRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    private final WorkloadContainerRepository workloadContainerRepository;

    public ExcelExportService(WorkloadContainerRepository workloadContainerRepository) {
        this.workloadContainerRepository = workloadContainerRepository;
    }


    public byte[] export() throws IOException {
        var containers = workloadContainerRepository.findAllWithAssociations();
        var data = prepareExportData(containers);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Нагрузка");

            // Стиль для заголовков
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Создание заголовков
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Наименование дисциплины",
                    "Тип нагрузки",
                    "Часы",
                    "Преподаватель",
                    "Группы"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Заполнение данными
            int rowNum = 1;
            for (WorkloadExportDTO item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getLessonName());
                row.createCell(1).setCellValue(item.getWorkloadTypes());
                row.createCell(2).setCellValue(item.getWorkloadHours());
                row.createCell(3).setCellValue(item.getTeacherName());
                row.createCell(4).setCellValue(item.getGroups());
            }

            // Автонастройка ширины колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private List<WorkloadExportDTO> prepareExportData(List<WorkloadContainer> containers) {
        return containers.stream()
                .map(this::convertToExportDTO)
                .collect(Collectors.toList());
    }

    private WorkloadExportDTO convertToExportDTO(WorkloadContainer wc) {
        // Наименование дисциплины
        String lessonName = wc.getLesson() != null ? wc.getLesson().getName() : "N/A";

        // Уникальные типы нагрузки
        Set<String> workloadTypes = wc.getWorkloads().stream()
                .map(w -> w.getType().getTranslation())
                .collect(Collectors.toSet());

        // Часы нагрузки
        Integer hours = wc.getWorkloadHours();

        // Преподаватель
        String teacher = resolveTeacherName(wc);

        // Список групп
        String groups = wc.getWorkloads().stream()
                .map(Workload::getGroup)
                .filter(g -> g != null)
                .map(StudentsGroup::getName)
                .distinct()
                .collect(Collectors.joining(", "));

        return new WorkloadExportDTO(
                lessonName,
                String.join(", ", workloadTypes),
                hours,
                teacher,
                groups
        );
    }

    private String resolveTeacherName(WorkloadContainer wc) {
        if (wc.getPosition() != null &&
                wc.getPosition().getEmployee() != null) {
            return wc.getPosition().getEmployee().getName();
        }
        return "Не назначен";
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    @Data
    @AllArgsConstructor
    static
    class WorkloadExportDTO {
        private String lessonName;
        private String workloadTypes;
        private Integer workloadHours;
        private String teacherName;
        private String groups;
    }
}