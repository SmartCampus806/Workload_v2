package com.main.workload.controllers;

import com.main.workload.exceptions.FileParsingException;
import com.main.workload.services.CompetencyParserService;
import com.main.workload.services.EmployeeParserService;
import com.main.workload.services.WorkloadImportProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
@Tag(name = "Upload data API", description = "API для загрузки и обработки файлов")
public class UploaderController {
    private final WorkloadImportProcessor parserService;
    private final EmployeeParserService employeeParserService;
    private final CompetencyParserService competencyParserService;

    @Autowired
    public UploaderController(WorkloadImportProcessor parserService, EmployeeParserService employeeParserService, CompetencyParserService competencyParserService) {
        this.parserService = parserService;
        this.employeeParserService = employeeParserService;
        this.competencyParserService = competencyParserService;
    }

    @PostMapping(value = "/workload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить файл нагрузки")
    public ResponseEntity<String> handleFileUpload(
            @Parameter(description = "Файл для загрузки", required = true)
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Файл пустой");
        }

        try {
            parserService.process(file.getInputStream());
            return ResponseEntity.ok("Файл успешно обработан");
        } catch (FileParsingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при обработке файла: " + e.getMessage());
        }
    }

    @PostMapping(value = "/employee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить файл преподавателей")
    public ResponseEntity<String> handleFileUploadEmployee(
            @Parameter(description = "Файл для загрузки", required = true)
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Файл пустой");
        }

        try {
            employeeParserService.parse(file.getInputStream());
            return ResponseEntity.ok("Файл успешно обработан");
        } catch (FileParsingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при обработке файла: " + e.getMessage());
        }
    }

    @PostMapping(value = "/competencies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить файл компетенций")
    public ResponseEntity<String> handleFileUploadCompetency(
            @Parameter(description = "Файл для загрузки", required = true)
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Файл пустой");
        }

        try {
            competencyParserService.parse(file.getInputStream());
            return ResponseEntity.ok("Файл успешно обработан");
        } catch (FileParsingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при обработке файла: " + e.getMessage());
        }
    }
}
