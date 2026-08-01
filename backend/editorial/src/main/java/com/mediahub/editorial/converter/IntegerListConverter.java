package com.mediahub.editorial.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class IntegerListConverter
        implements AttributeConverter<List<Integer>, String> {

    @Override
    public String convertToDatabaseColumn(List<Integer> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream()
                   .map(String::valueOf)
                   .collect(Collectors.joining(","));
    }

    @Override
    public List<Integer> convertToEntityAttribute(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
                     .map(Integer::parseInt)
                     .collect(Collectors.toList());
    }
}
