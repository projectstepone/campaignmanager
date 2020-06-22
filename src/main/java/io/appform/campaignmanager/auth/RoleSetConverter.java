package io.appform.campaignmanager.auth;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@Converter
public class RoleSetConverter implements AttributeConverter<Set<String>, String> {
    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        return Joiner.on(",").join(attribute);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        return new HashSet<>(Splitter.on(",").omitEmptyStrings().splitToList(dbData));
    }
}
