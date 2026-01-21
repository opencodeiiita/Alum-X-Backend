package com.opencode.alumxbackend.groupchatmessages.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageSearchResponse {

    private Long groupId;
    private List<GroupMessageResponse> messages;
    private Long totalMatches;
    private Integer page;
    private Integer totalPages;
}
