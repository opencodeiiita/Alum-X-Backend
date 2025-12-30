package com.opencode.alumxbackend.groupchatmessages.service;

import com.opencode.alumxbackend.groupchatmessages.dto.GroupMessageResponse;
import com.opencode.alumxbackend.groupchatmessages.dto.SendGroupMessageRequest;

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
    List<GroupMessageResponse> getAllGroupMessages(Long groupId);
}
