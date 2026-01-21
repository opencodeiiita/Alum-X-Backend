package com.opencode.alumxbackend.groupchatreadreceipt.service;

import com.opencode.alumxbackend.groupchatreadreceipt.dto.GroupReadResponse;

public interface GroupReadService {

    GroupReadResponse updateLastRead(Long groupId, Long userId, Long lastReadMessageId);

    GroupReadResponse getLastReadMessage(Long groupId, Long userId);
}
