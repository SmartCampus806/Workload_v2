package com.main.workload.services;

import com.main.workload.entities.*;
import com.main.workload.repositories.AcademicLoadRepository;
import com.main.workload.repositories.LessonRepository;
import com.main.workload.repositories.StudentsGroupRepository;
import com.main.workload.repositories.WorkloadContainerRepository;
import com.main.workload.utils.Pair;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkloadImportProcessor {
    private final StudentsGroupRepository studentsGroupRepository;
    private final WorkloadParserService workloadParserService;
    private final LessonRepository lessonRepository;
    private final WorkloadContainerRepository workloadContainerRepository;

    @Autowired
    public WorkloadImportProcessor(StudentsGroupRepository studentsGroupRepository,
                                   WorkloadParserService workloadParserService,
                                   LessonRepository lessonRepository, WorkloadContainerRepository workloadContainerRepository) {
        this.studentsGroupRepository = studentsGroupRepository;
        this.workloadParserService = workloadParserService;
        this.lessonRepository = lessonRepository;
        this.workloadContainerRepository = workloadContainerRepository;
    }

    public void process(@NonNull InputStream inputStream) {

        List<AcademicLoad> academicLoads = workloadParserService.parse(inputStream);
        Map<String, StudentsGroup> studentsGroups = new HashMap<>();

        Map<WorkloadGroupingKey, List<AcademicLoad>> groupedWorkload = groupBySubjectAndSemester(academicLoads);

        //TODO: удалить старые workload и workloadContainer
        List<WorkloadContainer> containers = groupedWorkload.values().stream()
                .flatMap(obj -> makeWorkloadsFromAcademicLoad(obj, studentsGroups).stream())
                .toList();

        workloadContainerRepository.saveAll(containers);
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
        
        StudentsGroup group = studentsGroupRepository.findByName(name)
                .orElse(studentsGroupRepository.save(new StudentsGroup(name, students)));
 
        if (!group.getStudentsCount().equals(students)) {
            group.setStudentsCount(students);
            group = studentsGroupRepository.save(group);
        
        }
        
        groups.put(group.getName(), group);
        return group;
    }
    private List<WorkloadContainer> makeWorkloadsFromAcademicLoad(List<AcademicLoad> groupedAcademicLoads, Map<String, StudentsGroup> groupMap) {
        Lesson lesson = checkLesson(groupedAcademicLoads.getFirst().getSubject(), groupedAcademicLoads.getFirst().getSemester());

        List<WorkloadContainer> res = new ArrayList<>();
        WorkloadContainer lectureContainer = new WorkloadContainer(lesson);

        for (var academicLoad : groupedAcademicLoads) {
            StudentsGroup group = checkGroup(academicLoad.getGroupName(), academicLoad.getStudents(), groupMap);
            
            if (academicLoad.getLecturesPlan() != null) {
                var workload = new Workload(Workload.WorkloadType.LECTURE, academicLoad.getConsult(), group);
                lectureContainer.getWorkloads().add(workload);
                workload.setContainer(lectureContainer);
            }
            if (academicLoad.getConsult() != null) {
                var workload = new Workload(Workload.WorkloadType.CONSULT, academicLoad.getConsult(), group);
                lectureContainer.getWorkloads().add(workload);
                workload.setContainer(lectureContainer);
            }
            if (academicLoad.getRating() != null) {
                var workload = new Workload(Workload.WorkloadType.RATING, academicLoad.getRating(), group);
                lectureContainer.getWorkloads().add(workload);
                workload.setContainer(lectureContainer);
            }
            if (academicLoad.getCredit() != null) {
                var workload = new Workload(Workload.WorkloadType.CREDIT, academicLoad.getCredit(), group);
                lectureContainer.getWorkloads().add(workload);
                workload.setContainer(lectureContainer);
            }
            if (academicLoad.getExam() != null) {
                var workload = new Workload(Workload.WorkloadType.EXAM, academicLoad.getExam(), group);
                lectureContainer.getWorkloads().add(workload);
                workload.setContainer(lectureContainer);
            }

            List<Workload> tmpWorkloadList = new ArrayList<>();
            if (academicLoad.getLabsLoad() != null) {
                var workload = new Workload(Workload.WorkloadType.LABORATORY_WORK, academicLoad.getLabsLoad(), group);
                tmpWorkloadList.add(workload);
            }
            if (academicLoad.getPracticalsLoad() != null) {
                var workload = new Workload(Workload.WorkloadType.PRACTICE, academicLoad.getPracticalsLoad(), group);
                tmpWorkloadList.add(workload);
            }
            if (academicLoad.getCourseWork() != null) {
                var workload = new Workload(Workload.WorkloadType.COURSE_WORK, academicLoad.getCourseWork(), group);
                tmpWorkloadList.add(workload);
            }
            if (academicLoad.getCourseProject() != null) {
                var workload = new Workload(Workload.WorkloadType.COURSE_PROJECT, academicLoad.getCourseProject(), group);
                tmpWorkloadList.add(workload);
            }

            // TODO: Ручное распределение
            if (academicLoad.getDiploma() != null) {
                var workload = new Workload(Workload.WorkloadType.DIPLOMA, academicLoad.getDiploma(), group);
                tmpWorkloadList.add(workload);
            }
            if (academicLoad.getOther() != null) {
                var workload = new Workload(Workload.WorkloadType.OTHER, academicLoad.getOther(), group);
                tmpWorkloadList.add(workload);
            }
            if (academicLoad.getKsr() != null) {
                var workload = new Workload(Workload.WorkloadType.KSR, academicLoad.getKsr(), group);
                tmpWorkloadList.add(workload);
            }

            for (var workload : tmpWorkloadList) {
                WorkloadContainer container = new WorkloadContainer(lesson);
                container.setWorkloads(List.of(workload));
                workload.setContainer(container);
                res.add(container);
            }
        }
        res.add(lectureContainer);
        return res;
    }
    
    @Data
    @AllArgsConstructor
    public class WorkloadGroupingKey {
        private String subject;
        private Integer semester;
    }
}
