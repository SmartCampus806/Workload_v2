package com.main.workload.services.distribution;

import com.main.workload.entities.Employee;
import com.main.workload.entities.EmployeePosition;
import com.main.workload.entities.Lesson;
import com.main.workload.entities.WorkloadContainer;
import com.main.workload.utils.Pair;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkloadDistributionService {

    public List<Pair<WorkloadContainer, EmployeePosition>> distributeWorkload(
            List<WorkloadContainer> containers, List<EmployeePosition> positions) {

        int numContainers = containers.size();
        int numPositions = positions.size();

        // Проверка входных данных
        if (numContainers == 0 || numPositions == 0) {
            throw new IllegalArgumentException("Списки контейнеров или позиций не могут быть пустыми");
        }

        // Матрица компетенций
        boolean[][] canTeach = new boolean[numContainers][numPositions];
        for (int i = 0; i < numContainers; i++) {
            Lesson lesson = containers.get(i).getLesson();
            for (int j = 0; j < numPositions; j++) {
                Employee employee = positions.get(j).getEmployee();
                canTeach[i][j] = employee.getAvailableLessons().contains(lesson);
            }
        }

        // Создание модели
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        // Переменные x_{i,j} (бинарные)
        Variable[][] x = new Variable[numContainers][numPositions];
        for (int i = 0; i < numContainers; i++) {
            for (int j = 0; j < numPositions; j++) {
                String varName = "x_" + i + "_" + j;
                if (canTeach[i][j]) {
                    x[i][j] = model.addVariable(varName).lower(0).upper(1).integer(true);
                } else {
                    x[i][j] = model.addVariable(varName).lower(0).upper(0).integer(true);
                }
            }
        }

        // Переменная z для минимизации максимальной загрузки
        Variable z = model.addVariable("z").lower(0).weight(1.0); // Задаём вес для минимизации z

        // Ограничение: Каждый контейнер назначается ровно одному преподавателю
        for (int i = 0; i < numContainers; i++) {
            Expression expr = model.addExpression("container_" + i).level(1);
            for (int j = 0; j < numPositions; j++) {
                expr.set(x[i][j], 1);
            }
        }

        // Ограничение: Ограничение загрузки преподавателя
        for (int j = 0; j < numPositions; j++) {
            double fullWorkload = positions.get(j).getFullWorkload();
            Expression expr = model.addExpression("position_" + j).upper(fullWorkload);
            for (int i = 0; i < numContainers; i++) {
                double workloadHours = containers.get(i).getWorkloadHours();
                expr.set(x[i][j], workloadHours);
            }
        }

        // Ограничение: Связь с z (минимизация максимальной относительной загрузки)
        for (int j = 0; j < numPositions; j++) {
            double fullWorkload = positions.get(j).getFullWorkload();
            Expression expr = model.addExpression("load_" + j).upper(z);
            for (int i = 0; i < numContainers; i++) {
                double workloadHours = containers.get(i).getWorkloadHours();
                expr.set(x[i][j], workloadHours / fullWorkload);
            }
        }

        // Решение задачи
        Optimisation.Result result = model.minimise();
        if (!result.getState().isFeasible()) {
            throw new RuntimeException("Не удалось распределить нагрузку: решение не найдено");
        }

        // Формирование результата
        List<Pair<WorkloadContainer, EmployeePosition>> assignments = new ArrayList<>();
        for (int i = 0; i < numContainers; i++) {
            boolean assigned = false;
            for (int j = 0; j < numPositions; j++) {
                if (x[i][j].getValue().intValue() == 1) {
                    assignments.add(new Pair<>(containers.get(i), positions.get(j)));
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                throw new RuntimeException("Контейнер " + i + " не был распределён");
            }
        }

        // Проверка, что все контейнеры распределены
        if (assignments.size() != numContainers) {
            throw new RuntimeException("Не все контейнеры были распределены");
        }

        return assignments;
    }
}