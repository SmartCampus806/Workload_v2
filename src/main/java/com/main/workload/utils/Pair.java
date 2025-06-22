package com.main.workload.utils;

import com.main.workload.entities.AcademicLoad;
import com.main.workload.entities.StudentsGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Pair {
    List<AcademicLoad> first;
    List<StudentsGroup> second;
}
