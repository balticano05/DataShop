package com.datashop.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        return UserRole.from(dbData);
    }
}
