package com.opencode.alumxbackend.groupchatmessages.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMessageSearchRequest {

    private String query;
    private Integer page;
    private Integer size;
}
