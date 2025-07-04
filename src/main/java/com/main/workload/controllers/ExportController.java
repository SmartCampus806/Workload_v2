package com.main.workload.controllers;

import com.main.workload.dtos.EmployeeWithPositionsDTO;
import com.main.workload.entities.Employee;
import com.main.workload.entities.EmployeePosition;
import com.main.workload.services.EmployeeService;
import com.main.workload.services.ExcelExportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Валидация", description = "API для проверки валидности данных и исключения ручных ошибок")
public class ExportController {

    private final ExcelExportService excelExportService;

    @Autowired
    public ExportController(ExcelExportService excelExportService) {

        this.excelExportService = excelExportService;
    }

    @GetMapping("/export/workload")
    public ResponseEntity<ByteArrayResource> checkCompetences() {
        try {
            byte[] excelBytes = excelExportService.export();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=workload_export.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new ByteArrayResource(excelBytes));
        } catch (IOException ex) {
            return ResponseEntity.status(500).body(null);
        }
    }

}


