package com.main.workload.services.distribution;

import com.main.workload.entities.EmployeePosition;
import com.main.workload.entities.WorkloadContainer;
import com.main.workload.repositories.EmployeePositionRepository;
import com.main.workload.repositories.WorkloadContainerRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional()
public class WorkloadAssignmentManagerService {

    @Autowired
    private WorkloadContainerRepository workloadContainerRepository;

    @Autowired
    private EmployeePositionRepository employeePositionRepository;

    @Autowired
    private WorkloadDistributionServiceGenetic workloadDistributionServiceGenetic;

    @Data
    public static class DistributionResult {
        private List<WorkloadDistributionServiceGenetic.WorkloadAssignment> assignments;
        private DistributionQuality quality;
    }

    @Data
    public static class DistributionQuality {
        private double variance; // Дисперсия относительной нагрузки
        private boolean fullyAssigned; // Все ли контейнеры распределены
        private boolean allCompetent; // Все ли назначения соответствуют компетенциям
        private String summary; // Краткое описание качества на русском
    }

    private static final Set<EmployeePosition.Post> EXCLUDED_POSTS = Set.of(
            EmployeePosition.Post.ENGINEER,
            EmployeePosition.Post.ENGINEER_CATEGORY_1,
            EmployeePosition.Post.ENGINEER_CATEGORY_2,
            EmployeePosition.Post.TECHNICIAN,
            EmployeePosition.Post.LEADING_METHODOLOGY_SPECIALIST,
            EmployeePosition.Post.LEADING_ENGINEER,
            EmployeePosition.Post.LEADING_ELECTRONICS_ENGINEER
    );

    public DistributionResult assignWorkload() {

        workloadContainerRepository.setAllPositionsToNull();

        // Получаем данные из базы
        List<WorkloadContainer> containers = workloadContainerRepository.findAll().stream()
                .filter(x -> !x.getLesson().getQualifiedEmployees().isEmpty())
                .toList();
        List<EmployeePosition> positions = employeePositionRepository.findByActiveTrueAndStructuralDivision(
                EmployeePosition.StructuralDivision.DEPARTMENT_806).stream()
                .filter(position -> !EXCLUDED_POSTS.contains(position.getPost()))
                .toList();

        if (containers.isEmpty()) {
            throw new RuntimeException("Не найдено контейнеров нагрузки");
        }
        if (positions.isEmpty()) {
            throw new RuntimeException("Не найдено активных позиций для кафедры 806");
        }

        // Вызываем метод распределения
        List<WorkloadDistributionServiceGenetic.WorkloadAssignment> assignments =
                workloadDistributionServiceGenetic.distributeWorkload(containers, positions);

        // Cохраняем результат
        List<WorkloadContainer> updatedContainers = assignments.stream().map(x -> {
            WorkloadContainer container = x.getContainer();
            container.setPosition(x.getPosition());
            return container;
            }).toList();
        workloadContainerRepository.saveAll(updatedContainers);

        // Оцениваем качество распределения
        DistributionQuality quality = evaluateQuality(assignments, positions);

        // Формируем результат
        DistributionResult result = new DistributionResult();
        result.setAssignments(assignments);
        result.setQuality(quality);

        return result;
    }

    private DistributionQuality evaluateQuality(List<WorkloadDistributionServiceGenetic.WorkloadAssignment> assignments,
                                                List<EmployeePosition> positions) {
        DistributionQuality quality = new DistributionQuality();

        // Карта нагрузки по сотрудникам
        Map<EmployeePosition, Double> workloadMap = new HashMap<>();
        for (WorkloadDistributionServiceGenetic.WorkloadAssignment assignment : assignments) {
            EmployeePosition position = assignment.getPosition();
            double workloadHours = assignment.getContainer().getWorkloadHours();
            workloadMap.put(position, workloadMap.getOrDefault(position, 0.0) + workloadHours);
        }

        // Проверяем полноту распределения
        quality.setFullyAssigned(assignments.stream().noneMatch(x -> x.getContainer().getPosition() == null));

        // Проверяем соответствие компетенциям
        boolean allCompetent = true;
        for (WorkloadDistributionServiceGenetic.WorkloadAssignment assignment : assignments) {
            if (!assignment.getPosition().getEmployee().getAvailableLessons()
                    .contains(assignment.getContainer().getLesson())) {
                allCompetent = false;
                break;
            }
        }
        quality.setAllCompetent(allCompetent);

        // Вычисляем дисперсию относительной нагрузки
        double maxWorkload = positions.stream()
                .mapToDouble(EmployeePosition::getFullWorkload)
                .max()
                .orElse(1.0);
        double meanRelativeWorkload = 0.0;
        int count = 0;
        for (Double workload : workloadMap.values()) {
            meanRelativeWorkload += workload / maxWorkload;
            count++;
        }
        meanRelativeWorkload /= count > 0 ? count : 1;

        double variance = 0.0;
        for (Double workload : workloadMap.values()) {
            double relativeWorkload = workload / maxWorkload;
            variance += Math.pow(relativeWorkload - meanRelativeWorkload, 2);
        }
        variance /= count > 0 ? count : 1;
        quality.setVariance(variance);

        // Формируем краткое описание на русском
        StringBuilder summary = new StringBuilder();
        summary.append("Качество распределения:\n");
        summary.append(String.format("\tДисперсия относительной нагрузки: %.4f (чем ниже, тем лучше)\n", variance));
        summary.append("\tВсе контейнеры распределены: ").append(quality.isFullyAssigned() ? "Да" : "Нет").append("\n");
        summary.append("\tВсе назначения соответствуют компетенциям: ").append(quality.isAllCompetent() ? "Да" : "Нет");
        quality.setSummary(summary.toString());

        return quality;
    }
}