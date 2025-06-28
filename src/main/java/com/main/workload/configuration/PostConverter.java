package com.main.workload.configuration;

import com.main.workload.entities.EmployeePosition;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PostConverter implements AttributeConverter<EmployeePosition.Post, String> {

    @Override
    public String convertToDatabaseColumn(EmployeePosition.Post post) {
        return post == null ? null : post.getDisplayName();
    }

    @Override
    public EmployeePosition.Post convertToEntityAttribute(String dbData) {
        return EmployeePosition.Post.fromDisplayName(dbData);
    }
}