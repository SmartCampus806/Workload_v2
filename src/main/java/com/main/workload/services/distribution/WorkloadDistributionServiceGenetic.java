package com.main.workload.services.distribution;

import com.main.workload.entities.EmployeePosition;
import com.main.workload.entities.Lesson;
import com.main.workload.entities.WorkloadContainer;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkloadDistributionServiceGenetic {
    private static final int POPULATION_SIZE = 200;
    private static final int GENERATIONS = 1000;
    private static final double MUTATION_RATE = 0.01;

    @Data
    public static class WorkloadAssignment {
        private WorkloadContainer container;
        private EmployeePosition position;

        public WorkloadAssignment(WorkloadContainer container, EmployeePosition position) {
            this.container = container;
            this.position = position;
        }
    }

    public List<WorkloadAssignment> distributeWorkload(List<WorkloadContainer> containers, List<EmployeePosition> positions) {
        GeneticAlgorithm ga = new GeneticAlgorithm(containers, positions);
        Chromosome best = ga.run();
        return buildAssignments(best, containers, positions);
    }

    private List<WorkloadAssignment> buildAssignments(Chromosome chromosome, List<WorkloadContainer> containers, List<EmployeePosition> positions) {
        List<WorkloadAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < containers.size(); i++) {
            int positionIndex = chromosome.getGenes()[i];
            EmployeePosition position = positions.get(positionIndex);
            WorkloadContainer container = containers.get(i);
            assignments.add(new WorkloadAssignment(container, position));
        }
        return assignments;
    }

    @Getter
    private class Chromosome {
        private final int[] genes;
        @Setter
        private double fitness;

        public Chromosome(int size) {
            genes = new int[size];
            fitness = 0.0;
        }
    }

    private class GeneticAlgorithm {
        private List<WorkloadContainer> containers;
        private List<EmployeePosition> positions;
        private Random random;
        private Map<Integer, List<Integer>> competentPositionsMap;

        public GeneticAlgorithm(List<WorkloadContainer> containers, List<EmployeePosition> positions) {
            this.containers = new ArrayList<>(containers);
            this.positions = positions.stream()
                    .filter(ep -> ep.getActive() &&
                            ep.getStructuralDivision() == EmployeePosition.StructuralDivision.DEPARTMENT_806)
                    .collect(Collectors.toList());
            if (this.positions.isEmpty()) {
                throw new RuntimeException("No active EmployeePosition found for Department 806");
            }
            this.random = new Random();
            buildCompetentPositionsMap();
        }

        private void buildCompetentPositionsMap() {
            competentPositionsMap = new HashMap<>();
            for (int i = 0; i < containers.size(); i++) {
                WorkloadContainer container = containers.get(i);
                Lesson lesson = container.getLesson();
                List<Integer> competentIndices = new ArrayList<>();
                for (int j = 0; j < positions.size(); j++) {
                    EmployeePosition position = positions.get(j);
                    if (position.getEmployee().getAvailableLessons().contains(lesson)) {
                        competentIndices.add(j);
                    }
                }
                if (competentIndices.isEmpty()) {
                    throw new RuntimeException("No competent EmployeePosition for container: " + container.getId());
                }
                competentPositionsMap.put(i, competentIndices);
            }
        }

        public Chromosome run() {
            List<Chromosome> population = initializePopulation();
            Chromosome bestChromosome = null;
            for (int generation = 0; generation < GENERATIONS; generation++) {
                log.info("Run generation {}", generation);
                evaluatePopulation(population);
                Chromosome currentBest = population.stream()
                        .max(Comparator.comparingDouble(Chromosome::getFitness))
                        .orElse(null);
                if (bestChromosome == null || (currentBest != null && currentBest.getFitness() > bestChromosome.getFitness())) {
                    bestChromosome = currentBest;
                }
                List<Chromosome> newPopulation = new ArrayList<>();
                if (bestChromosome != null) {
                    newPopulation.add(bestChromosome);
                }
                while (newPopulation.size() < POPULATION_SIZE) {
                    Chromosome parent1 = selectParent(population);
                    Chromosome parent2 = selectParent(population);
                    Chromosome child = crossover(parent1, parent2);
                    mutate(child);
                    newPopulation.add(child);
                }
                population = newPopulation;
            }
            if (bestChromosome != null) {
                evaluatePopulation(population);
                return bestChromosome;
            } else {
                throw new RuntimeException("No valid chromosome found");
            }
        }

        private List<Chromosome> initializePopulation() {
            List<Chromosome> population = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                Chromosome chromosome = new Chromosome(containers.size());
                for (int j = 0; j < containers.size(); j++) {
                    List<Integer> competentIndices = competentPositionsMap.get(j);
                    int randomIndex = competentIndices.get(random.nextInt(competentIndices.size()));
                    chromosome.getGenes()[j] = randomIndex;
                }
                population.add(chromosome);
            }
            return population;
        }

        private void evaluatePopulation(List<Chromosome> population) {
            for (Chromosome chromosome : population) {
                double fitness = calculateFitness(chromosome);
                chromosome.setFitness(fitness);
            }
        }

        private double calculateFitness(Chromosome chromosome) {
            Map<EmployeePosition, Double> workloadMap = new HashMap<>();
            for (int i = 0; i < containers.size(); i++) {
                WorkloadContainer container = containers.get(i);
                int positionIndex = chromosome.getGenes()[i];
                EmployeePosition position = positions.get(positionIndex);
                double workload = container.getWorkloadHours();
                workloadMap.put(position, workloadMap.getOrDefault(position, 0.0) + workload);
            }

            double variance = 0.0;
            for (Map.Entry<EmployeePosition, Double> entry : workloadMap.entrySet()) {
                EmployeePosition position = entry.getKey();
                double assignedWorkload = entry.getValue();
                double maxWorkload = position.getFullWorkload();
                double relativeWorkload = assignedWorkload / maxWorkload;
                variance += Math.pow(relativeWorkload - 0.5, 2);
            }
            variance /= workloadMap.size();
            return 1.0 / (1.0 + variance);
        }

        private Chromosome selectParent(List<Chromosome> population) {
            double totalFitness = population.stream().mapToDouble(Chromosome::getFitness).sum();
            double randomValue = random.nextDouble() * totalFitness;
            double cumulativeFitness = 0.0;
            for (Chromosome chromosome : population) {
                cumulativeFitness += chromosome.getFitness();
                if (cumulativeFitness >= randomValue) {
                    return chromosome;
                }
            }
            return population.get(population.size() - 1);
        }

        private Chromosome crossover(Chromosome parent1, Chromosome parent2) {
            Chromosome child = new Chromosome(containers.size());
            int crossoverPoint = random.nextInt(containers.size());
            for (int i = 0; i < containers.size(); i++) {
                if (i < crossoverPoint) {
                    child.getGenes()[i] = parent1.getGenes()[i];
                } else {
                    child.getGenes()[i] = parent2.getGenes()[i];
                }
            }
            return child;
        }

        private void mutate(Chromosome chromosome) {
            for (int i = 0; i < chromosome.getGenes().length; i++) {
                if (random.nextDouble() < MUTATION_RATE) {
                    List<Integer> competentIndices = competentPositionsMap.get(i);
                    int randomIndex = competentIndices.get(random.nextInt(competentIndices.size()));
                    chromosome.getGenes()[i] = randomIndex;
                }
            }
        }
    }
}