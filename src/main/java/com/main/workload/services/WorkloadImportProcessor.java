package com.main.workload.services;

import com.main.workload.entities.*;
import com.main.workload.repositories.*;
import com.main.workload.utils.Pair;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.foreign.GroupLayout;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkloadImportProcessor {
    private final StudentsGroupRepository studentsGroupRepository;
    private final WorkloadParserService workloadParserService;
    private final LessonRepository lessonRepository;
    private final WorkloadContainerRepository workloadContainerRepository;
    private final WorkloadRepository workloadRepository;

    @Autowired
    public WorkloadImportProcessor(StudentsGroupRepository studentsGroupRepository,
                                   WorkloadParserService workloadParserService,
                                   LessonRepository lessonRepository, WorkloadContainerRepository workloadContainerRepository, WorkloadRepository workloadRepository) {
        this.studentsGroupRepository = studentsGroupRepository;
        this.workloadParserService = workloadParserService;
        this.lessonRepository = lessonRepository;
        this.workloadContainerRepository = workloadContainerRepository;
        this.workloadRepository = workloadRepository;
    }

    public void process(@NonNull InputStream inputStream) {

        List<AcademicLoad> academicLoads = workloadParserService.parse(inputStream);

        log.info(String.valueOf(academicLoads == null ? 0 : academicLoads.stream()
            .filter(Objects::nonNull)
            .mapToInt(x ->
                (x.getPracticalsLoad() != null ? x.getPracticalsLoad() : 0) +
                (x.getLabsLoad() != null ? x.getLabsLoad() : 0) +
                (x.getLecturesPlan() != null ? x.getLecturesPlan() : 0) +
                (x.getCourseProject() != null ? x.getCourseProject() : 0) +
                (x.getCourseWork() != null ? x.getCourseWork() : 0) +
                (x.getCredit() != null ? x.getCredit() : 0) +
                (x.getRating() != null ? x.getRating() : 0) +
                (x.getExam() != null ? x.getExam() : 0) +
                (x.getKsr() != null ? x.getKsr() : 0) +
                (x.getOther() != null ? x.getOther() : 0) +
                (x.getConsult() != null ? x.getConsult() : 0) +
                (x.getDiploma() != null ? x.getDiploma() : 0)
            )
            .sum()
        ));
        Map<String, StudentsGroup> studentsGroups = new HashMap<>();

        Map<WorkloadGroupingKey, List<AcademicLoad>> groupedWorkload = groupBySubjectAndSemester(academicLoads);

        workloadRepository.deactivateAll();
        List<WorkloadContainer> newContainers = groupedWorkload.values().stream()
                .flatMap(obj -> makeWorkloadsFromAcademicLoad(obj, studentsGroups).stream())
                .toList();
        workloadContainerRepository.saveAll(newContainers);
        workloadRepository.deleteAllInactive();
        workloadContainerRepository.deleteUnusedContainers();
    }

    private Map<WorkloadGroupingKey, List<AcademicLoad>> groupBySubjectAndSemester(List<AcademicLoad> academicLoads) {
        return academicLoads.stream()
                .collect(Collectors.groupingBy(load -> new WorkloadGroupingKey(load.getSubject(), load.getSemester())));
    }

    private Lesson checkLesson(String name, Integer semester) {
        Optional<Lesson> savedLesson = lessonRepository.findByNameAndSemester(name, semester);
        if (savedLesson.isEmpty())
            savedLesson = Optional.of(lessonRepository.save(new Lesson(name, semester)));

        return savedLesson.get();
    }
    
    private StudentsGroup checkGroup(String name, Integer students, Map<String, StudentsGroup> groups) {
        if (groups.containsKey(name))
            return groups.get(name);

        Optional<StudentsGroup> group = studentsGroupRepository.findByName(name);
        if (group.isEmpty())
            group = Optional.of(studentsGroupRepository.save(new StudentsGroup(name, students)));

        if (!group.get().getStudentsCount().equals(students)) {
            group.get().setStudentsCount(students);
            group = Optional.of(studentsGroupRepository.save(group.get()));
        
        }
        
        groups.put(group.get().getName(), group.get());
        return group.get();
    }
    private List<WorkloadContainer> makeWorkloadsFromAcademicLoad(List<AcademicLoad> groupedAcademicLoads, Map<String, StudentsGroup> groupMap) {
        Lesson lesson = checkLesson(groupedAcademicLoads.getFirst().getSubject(), groupedAcademicLoads.getFirst().getSemester());

        List<WorkloadContainer> res = new ArrayList<>();
        WorkloadContainer lectureContainer = new WorkloadContainer(lesson);

        for (var academicLoad : groupedAcademicLoads) {
            StudentsGroup group = checkGroup(academicLoad.getGroupName(), academicLoad.getStudents(), groupMap);
            
            if (academicLoad.getLecturesPlan() != null) {
                var exist = getExistContainer(lesson, academicLoad, group, academicLoad.getLecturesPlan(), Workload.WorkloadType.LECTURE);
                if (exist.isPresent() && lectureContainer.getId() == null) {
                    exist.get().getWorkloads().addAll(lectureContainer.getWorkloads());
                    lectureContainer = exist.get();
                }
                var workload = new Workload(Workload.WorkloadType.LECTURE, academicLoad.getLecturesPlan(), group);
                lectureContainer.addWorkload(workload);
            }
            if (academicLoad.getConsult() != null) {
                var exist = getExistContainer(lesson, academicLoad, group, academicLoad.getConsult(), Workload.WorkloadType.CONSULT);
                if (exist.isPresent() && lectureContainer.getId() == null) {
                    lectureContainer.getWorkloads().forEach(x -> exist.get().addWorkload(x));
                    lectureContainer = exist.get();
                }
                var workload = new Workload(Workload.WorkloadType.CONSULT, academicLoad.getConsult(), group);
                lectureContainer.addWorkload(workload);
            }
            if (academicLoad.getRating() != null) {
                var exist = getExistContainer(lesson, academicLoad, group, academicLoad.getRating(), Workload.WorkloadType.RATING);
                if (exist.isPresent() && lectureContainer.getId() == null) {
                    lectureContainer.getWorkloads().forEach(x -> exist.get().addWorkload(x));
                    lectureContainer = exist.get();
                }
                var workload = new Workload(Workload.WorkloadType.RATING, academicLoad.getRating(), group);
                lectureContainer.addWorkload(workload);
            }
            if (academicLoad.getCredit() != null) {
                var exist = getExistContainer(lesson, academicLoad, group, academicLoad.getCredit(), Workload.WorkloadType.CREDIT);
                if (exist.isPresent() && lectureContainer.getId() == null) {

                    lectureContainer.getWorkloads().forEach(x -> exist.get().addWorkload(x));

                    lectureContainer = exist.get();
                }
                var workload = new Workload(Workload.WorkloadType.CREDIT, academicLoad.getCredit(), group);
                lectureContainer.addWorkload(workload);
            }
            if (academicLoad.getExam() != null) {
                var exist = getExistContainer(lesson, academicLoad, group, academicLoad.getExam(), Workload.WorkloadType.EXAM);
                if (exist.isPresent() && lectureContainer.getId() == null) {
                    lectureContainer.getWorkloads().forEach(x -> exist.get().addWorkload(x));
                    lectureContainer = exist.get();
                }
                var workload = new Workload(Workload.WorkloadType.EXAM, academicLoad.getExam(), group);
                lectureContainer.addWorkload(workload);
            }

            // Отдельная нагрузка
            List<Workload> tmpWorkloadList = new ArrayList<>();
            if (academicLoad.getLabsLoad() != null) {
                processWorkload(lesson, academicLoad,group,academicLoad.getLabsLoad(), Workload.WorkloadType.LABORATORY_WORK)
                        .ifPresent(tmpWorkloadList::add);
            }
            if (academicLoad.getPracticalsLoad() != null) {
                processWorkload(lesson, academicLoad,group, academicLoad.getPracticalsLoad(), Workload.WorkloadType.PRACTICE)
                        .ifPresent(tmpWorkloadList::add);
            }
            if (academicLoad.getCourseWork() != null) {
                processWorkload(lesson, academicLoad,group, academicLoad.getCourseWork(), Workload.WorkloadType.COURSE_WORK)
                        .ifPresent(tmpWorkloadList::add);
            }
            if (academicLoad.getCourseProject() != null) {
                processWorkload(lesson, academicLoad,group, academicLoad.getCourseProject(), Workload.WorkloadType.COURSE_PROJECT)
                        .ifPresent(tmpWorkloadList::add);

            }

            // TODO: Ручное распределение
            if (academicLoad.getDiploma() != null) {
                processWorkload(lesson, academicLoad,group, academicLoad.getDiploma(), Workload.WorkloadType.DIPLOMA)
                        .ifPresent(tmpWorkloadList::add);
            }
            if (academicLoad.getOther() != null) {
                processWorkload(lesson, academicLoad,group, academicLoad.getOther(), Workload.WorkloadType.OTHER)
                        .ifPresent(tmpWorkloadList::add);
            }
            if (academicLoad.getKsr() != null) {
                processWorkload(lesson, academicLoad,group, academicLoad.getKsr(), Workload.WorkloadType.KSR)
                        .ifPresent(tmpWorkloadList::add);
            }

            for (var workload : tmpWorkloadList) {
                WorkloadContainer container = new WorkloadContainer(lesson);
                container.addWorkload(workload);
                res.add(container);
            }
        }

        // Проверям что lectureContainer не пуст, так как может не быть лекций и тп
        if (!lectureContainer.getWorkloads().isEmpty())
            res.add(lectureContainer);
        return res;
    }

    private Optional<Workload> processWorkload(Lesson lesson, AcademicLoad academicLoad, StudentsGroup group, Integer workload, Workload.WorkloadType type) {
        var existWorkload = workloadRepository.findByLessonAndWorkloadAndTypeAndGroup(lesson, workload,
                type, group);
        if(existWorkload.size() > 1) {
            log.error("Ошибка при попытке поиска Workload составной ключ не уникален.\n" +
                    "Workloads: {}, AcadecmicLoad: {}", existWorkload.stream().map(Workload::toString), academicLoad);
            throw new RuntimeException("Ошибка при попытке поиска Workload составной ключ не уникален.");
        }
        if (existWorkload.isEmpty()) {
            return Optional.of(new Workload(type, workload, group));
        }
        existWorkload.getFirst().setActive(true);
        workloadRepository.save(existWorkload.getFirst());
        return Optional.empty();
    }

    private Optional<WorkloadContainer> getExistContainer(@NonNull Lesson lesson, @NonNull AcademicLoad academicLoad, @NonNull StudentsGroup group, @NonNull Integer workload, @NonNull Workload.WorkloadType type) {
        var existWorkload = workloadRepository.findByLessonAndWorkloadAndTypeAndGroup(lesson, workload,
                type, group);

        if (existWorkload.size() > 1) {
            log.error("Ошибка при попытке поиска Workload составной ключ не уникален.\n" +
                    "Workloads: {}, AcadecmicLoad: {}", existWorkload.stream().map(Workload::toString), academicLoad);
            throw new RuntimeException("Ошибка при попытке поиска Workload составной ключ не уникален.");
        }
        if (existWorkload.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(existWorkload.getFirst().getContainer());

    }

    @Data
    @AllArgsConstructor
    public class WorkloadGroupingKey {
        private String subject;
        private Integer semester;
    }
}
