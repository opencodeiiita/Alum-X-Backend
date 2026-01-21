package com.opencode.alumxbackend.groupchat.dto;

import com.opencode.alumxbackend.groupchat.model.Participant;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatResponse {
    private Long groupId;
    private String name;
    private List<Participant> participants;
}
