package com.opencode.alumxbackend.groupchat.service;

import com.opencode.alumxbackend.groupchat.dto.GroupChatRequest;
import com.opencode.alumxbackend.groupchat.model.GroupChat;
import java.util.List;


public interface GroupChatService {

    GroupChat createGroup(GroupChatRequest request);

    GroupChat getGroupById(Long groupId);

    List<GroupChat> getGroupsForUser(Long userId);

    GroupChat addUserToGroup(Long groupId, Long userId);
    GroupChat removeUserFromGroup(Long groupId, Long userId);
}
