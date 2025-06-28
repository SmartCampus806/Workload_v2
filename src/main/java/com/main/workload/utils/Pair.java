package com.main.workload.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<A, B> {
    private final A first;
    private final B second;
}