package com.opencode.alumxbackend.users.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencode.alumxbackend.users.dto.AuraElement;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuraColorServiceImpl implements AuraColorService {

    private static final String DEFAULT_COLOR = "#808080";
    private Map<String, Object> colorMappings;

    @PostConstruct
    public void init() {
        loadColorMappings();
    }

    @SuppressWarnings("unchecked")
    private void loadColorMappings() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("aura-colors.json");
            InputStream inputStream = resource.getInputStream();
            colorMappings = mapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
            log.info("Loaded aura color mappings successfully");
        } catch (IOException e) {
            log.error("Failed to load aura-colors.json, using empty mappings", e);
            colorMappings = new HashMap<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getColor(String category, String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_COLOR;
        }

        Object categoryObj = colorMappings.get(category);
        if (categoryObj instanceof Map) {
            Map<String, Object> categoryColors = (Map<String, Object>) categoryObj;
            Object color = categoryColors.get(value);
            if (color instanceof String) {
                return (String) color;
            }
        }

        // Check nested careerFields
        Object careerFieldsObj = colorMappings.get("careerFields");
        if (careerFieldsObj instanceof Map) {
            Map<String, Object> careerFields = (Map<String, Object>) careerFieldsObj;
            for (Object fieldValue : careerFields.values()) {
                if (fieldValue instanceof Map) {
                    Map<String, Object> nestedColors = (Map<String, Object>) fieldValue;
                    Object color = nestedColors.get(value);
                    if (color instanceof String) {
                        return (String) color;
                    }
                }
            }
        }

        // Generate deterministic color for unknown values
        return generateDeterministicColor(value);
    }

    @Override
    public List<AuraElement> mapToColorAwareElements(String category, List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(value -> AuraElement.builder()
                        .name(value)
                        .color(getColor(category, value))
                        .build())
                .collect(Collectors.toList());
    }

    private String generateDeterministicColor(String value) {
        int hash = value.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        // Ensure colors are not too dark
        r = Math.max(80, r);
        g = Math.max(80, g);
        b = Math.max(80, b);
        return String.format("#%02X%02X%02X", r, g, b);
    }
}