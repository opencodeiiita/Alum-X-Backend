package com.opencode.alumxbackend.groupchatreadreceipt.service;

import com.opencode.alumxbackend.groupchatreadreceipt.dto.GroupReadRequest;
import com.opencode.alumxbackend.groupchatreadreceipt.dto.GroupReadResponse;
import com.opencode.alumxbackend.groupchatreadreceipt.model.GroupReadState;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.List;

public interface GroupReadService {

    GroupReadResponse updateLastRead(Long groupId, Long userId, Long lastReadMessageId);


    GroupReadResponse getLastReadMessage(Long groupId, Long userId);
}
