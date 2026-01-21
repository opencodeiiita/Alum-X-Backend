package com.opencode.alumxbackend.groupchatreadreceipt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupReadRequest {
    private Long userId;
    private Long lastReadMessageId;

}
