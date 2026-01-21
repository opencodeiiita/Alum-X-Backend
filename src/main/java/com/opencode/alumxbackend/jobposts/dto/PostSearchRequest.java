package com.opencode.alumxbackend.jobposts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostSearchRequest {
    
    private String keyword;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Integer page;
    private Integer size;
    
    public int getPageOrDefault() {
        return page != null && page >= 0 ? page : 0;
    }
    
    public int getSizeOrDefault() {
        if (size == null) return 10;
        if (size < 1) return 1;
        if (size > 100) return 100;
        return size;
    }
    
    public String getKeyword() {
        return keyword != null ? keyword.trim() : null;
    }
}
