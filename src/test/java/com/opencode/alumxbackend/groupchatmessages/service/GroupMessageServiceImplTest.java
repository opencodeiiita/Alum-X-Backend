package com.opencode.alumxbackend.groupchatmessages.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.opencode.alumxbackend.groupchat.model.GroupChat;
import com.opencode.alumxbackend.groupchat.model.Participant;
import com.opencode.alumxbackend.groupchat.repository.GroupChatRepository;
import com.opencode.alumxbackend.groupchatmessages.exception.InvalidMessageException;
import com.opencode.alumxbackend.groupchatmessages.exception.UserNotMemberException;
import com.opencode.alumxbackend.groupchatmessages.model.GroupMessage;
import com.opencode.alumxbackend.groupchatmessages.repository.GroupMessageRepository;

@ExtendWith(MockitoExtension.class)
class GroupMessageServiceImplTest {

    @Mock
    private GroupMessageRepository messageRepository;

    @Mock
    private GroupChatRepository groupChatRepository;

    @InjectMocks
    private GroupMessageServiceImpl service;

    private Long groupId;
    private Long messageId;
    private Long userId;

    @BeforeEach
    void setUp() {
        groupId = 1L;
        messageId = 10L;
        userId = 2L;
    }

    // ========== SUCCESS CASES ==========

    @Test
    @DisplayName("deleteMessage - should delete message when member and sender")
    void deleteMessage_WithValidData_DeletesMessage() {
        GroupChat group = groupWithMember(groupId, userId);
        GroupMessage message = groupMessage(messageId, groupId, userId);

        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        service.deleteMessage(groupId, messageId, userId);

        verify(messageRepository).delete(message);
    }

    // ========== FAILURE CASES ==========

    @Test
    @DisplayName("deleteMessage - should reject when user is not a group member")
    void deleteMessage_NonMember_ThrowsForbidden() {
        GroupChat group = groupWithMember(groupId, 99L);

        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> service.deleteMessage(groupId, messageId, userId))
                .isInstanceOf(UserNotMemberException.class);

        verify(messageRepository, never()).findById(any());
    }

    @Test
    @DisplayName("deleteMessage - should reject when user is not the sender")
    void deleteMessage_NotSender_ThrowsBadRequest() {
        GroupChat group = groupWithMember(groupId, userId);
        GroupMessage message = groupMessage(messageId, groupId, 99L);

        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> service.deleteMessage(groupId, messageId, userId))
                .isInstanceOf(InvalidMessageException.class);

        verify(messageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteMessage - should reject when message does not exist")
    void deleteMessage_MessageNotFound_ThrowsBadRequest() {
        GroupChat group = groupWithMember(groupId, userId);

        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMessage(groupId, messageId, userId))
                .isInstanceOf(InvalidMessageException.class);
    }

    @Test
    @DisplayName("deleteMessage - should reject when message belongs to another group")
    void deleteMessage_WrongGroup_ThrowsBadRequest() {
        GroupChat group = groupWithMember(groupId, userId);
        GroupMessage message = groupMessage(messageId, 99L, userId);

        when(groupChatRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> service.deleteMessage(groupId, messageId, userId))
                .isInstanceOf(InvalidMessageException.class);
    }

    private GroupChat groupWithMember(Long groupId, Long userId) {
        Participant participant = Participant.builder()
                .userId(userId)
                .username("member")
                .build();

        return GroupChat.builder()
                .groupId(groupId)
                .participants(List.of(participant))
                .build();
    }

    private GroupMessage groupMessage(Long messageId, Long groupId, Long senderId) {
        return GroupMessage.builder()
                .id(messageId)
                .groupId(groupId)
                .senderUserId(senderId)
                .senderUsername("user")
                .content("hi")
                .build();
    }
}
