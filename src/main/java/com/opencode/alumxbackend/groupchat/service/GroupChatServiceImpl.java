package com.opencode.alumxbackend.groupchat.service;

import com.opencode.alumxbackend.groupchat.dto.GroupChatRequest;
import com.opencode.alumxbackend.groupchat.model.GroupChat;
import com.opencode.alumxbackend.groupchat.model.Participant;
import com.opencode.alumxbackend.groupchat.model.Role;
import com.opencode.alumxbackend.groupchat.repository.GroupChatRepository;
import com.opencode.alumxbackend.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatServiceImpl implements  GroupChatService {
    private final GroupChatRepository repository;
    private final UserRepository userRepository;

    @Override
    public GroupChat createGroup(GroupChatRequest request) {
        GroupChat group = GroupChat.builder()
                .groupName(request.getName())
                .ownerId(request.getOwnerId())
                .createdAt(LocalDateTime.now())
                .build();

        Set<Long> userIds = request.getParticipants().stream()
                .map(p -> p.getUserId())
                .collect(Collectors.toSet());

        long count = userRepository.countByIdIn(userIds);
        if (count != userIds.size()) {
            throw new RuntimeException("One or more users do not exist");
        }

        boolean ownerPresent = request.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(request.getOwnerId()));

        if (!ownerPresent) {
            throw new RuntimeException("Owner must be present in participants list");
        }


        // Map DTO participants -> Participant entity
        List<Participant> participants = request.getParticipants().stream()
                .map(p -> {
                    return Participant.builder()
                            .userId(p.getUserId())
                            .username(p.getUsername())
                            .role(p.getUserId().equals(request.getOwnerId())
                                    ? Role.OWNER
                                    : Role.MEMBER)
                            .groupChat(group)
                            .build();

                }).collect(Collectors.toList());

        group.setParticipants(participants);

        // Save group along with participants (cascade)
        return repository.save(group);
    }


    @Override
    public GroupChat getGroupById(Long groupId) {
        return repository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }



    @Override
    public List<GroupChat> getGroupsForUser(Long userId) {
        return repository.findGroupsByUserId(userId);
    }
}
