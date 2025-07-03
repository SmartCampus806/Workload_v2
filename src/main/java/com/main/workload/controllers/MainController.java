package com.main.workload.controllers;

import com.main.workload.repositories.EmployeePositionRepository;
import com.main.workload.repositories.WorkloadContainerRepository;
import com.main.workload.services.distribution.WorkloadAssignmentManagerService;
import com.main.workload.services.distribution.WorkloadDistributionServiceGenetic;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Main", description = "Main")
@RequiredArgsConstructor
public class MainController {

    private final EmployeePositionRepository employeePositionRepository;
    private final WorkloadContainerRepository workloadContainerRepository;
    private final WorkloadAssignmentManagerService workloadAssignmentManagerService;

    @PostMapping("/distribute")
    public void distributeWorkload() {
        WorkloadAssignmentManagerService.DistributionResult result = workloadAssignmentManagerService.assignWorkload();
        System.out.println(result.getQuality().getSummary());

        Map<Long, Integer> temp = new HashMap<>();
        for (WorkloadDistributionServiceGenetic.WorkloadAssignment assignment : result.getAssignments()) {
            Long pos_id = assignment.getPosition().getId();
            if (temp.containsKey(pos_id))
                temp.put(pos_id, assignment.getContainer().getWorkloadHours() + temp.get(pos_id));
            else
                temp.put(pos_id, assignment.getContainer().getWorkloadHours());
        }

        long sum = 0L;
        for (var pos_id : temp.keySet()) {
            var pos = employeePositionRepository.findById(pos_id);
            var current_workload = temp.get(pos_id);
            var planned_workload =  pos.get().getFullWorkload();
            var employee_name = pos.get().getEmployee().getName();
            var pp = ((double)(current_workload / planned_workload) * 100);
            if (current_workload > planned_workload) {
                sum += current_workload - planned_workload.intValue();
                System.out.println("Перегружен" + employee_name + " - " + pp);
            }
            if (pp < 50) {
                System.out.println("Недогружен" + employee_name + " - " + pp);
            }
        }
        System.out.println("\nСуммарная переработка = " + sum);


        for (WorkloadDistributionServiceGenetic.WorkloadAssignment assignment : result.getAssignments()) {
            System.out.println("Контейнер ID: " + assignment.getContainer().getId() +
                    ", Назначен: " + assignment.getPosition().getEmployee().getName());
        }
    }
}
