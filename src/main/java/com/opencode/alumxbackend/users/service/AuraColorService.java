package com.opencode.alumxbackend.users.service;

import com.opencode.alumxbackend.users.dto.AuraElement;

import java.util.List;

public interface AuraColorService {
    String getColor(String category, String value);
    List<AuraElement> mapToColorAwareElements(String category, List<String> values);
}