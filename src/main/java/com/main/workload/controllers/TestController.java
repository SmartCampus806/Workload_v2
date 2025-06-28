package com.main.workload.controllers;

import com.main.workload.entities.Workload;
import com.main.workload.repositories.WorkloadRepository;
import com.main.workload.services.WorkloadImportProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Test API", description = "Test API Workaload")
public class TestController {

    private final WorkloadRepository workloadRepository;

    @Autowired
    public TestController(WorkloadRepository workloadRepository) {

        this.workloadRepository = workloadRepository;
    }

}
