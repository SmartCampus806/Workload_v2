package com.main.workload.utils;

import lombok.NonNull;

public class NameFormatter {

    public static @NonNull String formatFullName(@NonNull String input) {
        if (input.isBlank())
            return "";

        String[] parts = input.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(capitalize(part)).append(" ");
            }
        }

        return result.toString().trim();
    }

    private static @NonNull String capitalize(@NonNull String word) {
        if (word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}

