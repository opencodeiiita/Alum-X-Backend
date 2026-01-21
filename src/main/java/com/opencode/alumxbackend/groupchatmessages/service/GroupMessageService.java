package com.opencode.alumxbackend.groupchatmessages.service;

import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageSearchRequest;
import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageSearchResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.SendGroupMessageRequest;

import org.springframework.data.domain.Page;

import java.util.List;

public interface GroupMessageService {

    GroupMessageResponse sendMessage(
            Long groupId,
            SendGroupMessageRequest request
    );

    List<GroupMessageResponse> fetchMessages(
            Long groupId,
            Long userId
    );
    
    Page<GroupMessageResponse> getGroupMessagesWithPagination(Long groupId, Long userId, int page, int size);
    
    void deleteMessage(Long groupId, Long messageId, Long userId);

    GroupMessageSearchResponse searchForMessage(Long groupId, Long userId, GroupMessageSearchRequest request);

}
