package com.opencode.alumxbackend.groupchatreadreceipt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupReadResponse {
    private Long userId;
    private Long lastReadMessageId;

}
