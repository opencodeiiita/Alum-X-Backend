package com.opencode.alumxbackend.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to deserialize Spring Data Page responses in WebClient tests.
 * Spring's Page interface cannot be directly deserialized by Jackson, so this
 * class provides the necessary Jackson annotations for proper JSON deserialization.
 * 
 * <p>This is specifically used in integration tests where REST endpoints return
 * paginated responses and we need to deserialize them using WebClient.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestResponsePage<T> extends PageImpl<T> {
    
    /**
     * Main constructor used by Jackson for JSON deserialization.
     * Converts JSON properties into a Spring Data Page object.
     *
     * @param content the list of items in this page
     * @param number the current page number (0-indexed)
     * @param size the size of the page
     * @param totalElements the total number of elements across all pages
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestResponsePage(@JsonProperty("content") List<T> content,
                           @JsonProperty("number") int number,
                           @JsonProperty("size") int size,
                           @JsonProperty("totalElements") Long totalElements) {
        super(content, PageRequest.of(number, size), totalElements == null ? 0 : totalElements);
    }
    
    public RestResponsePage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
    
    public RestResponsePage(List<T> content) {
        super(content);
    }
    
    public RestResponsePage() {
        super(new ArrayList<>());
    }
}
