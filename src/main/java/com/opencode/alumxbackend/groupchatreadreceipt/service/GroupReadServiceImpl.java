package com.opencode.alumxbackend.groupchatreadreceipt.service;

import com.opencode.alumxbackend.groupchatreadreceipt.dto.GroupReadRequest;
import com.opencode.alumxbackend.groupchatreadreceipt.dto.GroupReadResponse;
import com.opencode.alumxbackend.groupchatreadreceipt.model.GroupReadState;
import com.opencode.alumxbackend.groupchatreadreceipt.repository.GroupReadStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupReadServiceImpl implements GroupReadService {
    private final GroupReadStateRepository repository;

    @Override
    public GroupReadResponse updateLastRead(Long groupId, Long userId, Long lastReadMessageId) {

        GroupReadState state = repository.findByGroupIdAndUserId(groupId, userId)
                .orElse(GroupReadState.builder()
                        .groupId(groupId)
                        .userId(userId)
                        .lastReadMessageId(lastReadMessageId)
                        .build());

        if (state.getLastReadMessageId() == null || lastReadMessageId > state.getLastReadMessageId()) {
            state.setLastReadMessageId(lastReadMessageId);
            repository.save(state);
        }

        return new GroupReadResponse(state.getUserId(), state.getLastReadMessageId());
    }

    @Override
    public GroupReadResponse getLastReadMessage(Long groupId, Long userId) {
        return repository.findByGroupIdAndUserId(groupId, userId)
                .map(state -> new GroupReadResponse(state.getUserId(), state.getLastReadMessageId()))
                .orElse(new GroupReadResponse(userId, null));
    }
}
