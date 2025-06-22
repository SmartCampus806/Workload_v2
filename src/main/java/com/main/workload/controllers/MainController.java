package com.main.workload.controllers;

import com.main.workload.exceptions.FileParsingException;
import com.main.workload.services.WorkloadParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@Tag(name = "Workload API", description = "API для загрузки и обработки файлов")
public class MainController {

    private final WorkloadParserService parserService;

    @Autowired
    public MainController(WorkloadParserService parserService) {
        this.parserService = parserService;
    }

    @Operation(summary = "Загрузить файл", description = "Загружает и обрабатывает файл с данными")
    @PostMapping("/upload")
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

    @Operation(summary = "Приветственное сообщение", description = "Простая проверка доступности API")
    @GetMapping("/hello")
    public String hello() {
        return "Hello, world!";
    }
}
