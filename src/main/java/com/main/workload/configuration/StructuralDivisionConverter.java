package com.main.workload.configuration;

import com.main.workload.entities.EmployeePosition;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StructuralDivisionConverter implements AttributeConverter<EmployeePosition.StructuralDivision, String> {

    @Override
    public String convertToDatabaseColumn(EmployeePosition.StructuralDivision structuralDivision) {
        return structuralDivision == null ? null : structuralDivision.getDisplayName();
    }

    @Override
    public EmployeePosition.StructuralDivision convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (EmployeePosition.StructuralDivision division : EmployeePosition.StructuralDivision.values()) {
            if (division.getDisplayName().equals(dbData)) {
                return division;
            }
        }
        throw new IllegalArgumentException("Unknown structural division: " + dbData);
    }
}