package com.opencode.alumxbackend.groupchat.service;

import com.opencode.alumxbackend.auth.security.UserPrincipal;
import com.opencode.alumxbackend.groupchat.dto.GroupChatRequest;
import com.opencode.alumxbackend.groupchat.model.GroupChat;
import com.opencode.alumxbackend.groupchat.model.Participant;
import com.opencode.alumxbackend.groupchat.model.ParticipantRole;
import com.opencode.alumxbackend.groupchat.repository.GroupChatRepository;
import com.opencode.alumxbackend.groupchat.repository.ParticipantRepository;
import com.opencode.alumxbackend.users.model.User;
import com.opencode.alumxbackend.users.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupChatServiceImpl implements  GroupChatService {
    private final GroupChatRepository repository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

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
                                    ? ParticipantRole.OWNER
                                    : ParticipantRole.MEMBER)
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


    @Override
    public GroupChat addUserToGroup(Long groupId, Long userId) {
        // Get current user (from JWT)
        UserPrincipal principal = (UserPrincipal)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        Long requesterId = principal.getId();

        GroupChat group = repository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Participant requester = participantRepository
                .findByGroupChat_GroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() ->
                        new AccessDeniedException("You are not a member of this group")
                );

        if (requester.getRole() != ParticipantRole.OWNER &&
            requester.getRole() != ParticipantRole.ADMIN) {
            throw new AccessDeniedException("Only OWNER or ADMIN can add users");
        }

        // New member
        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // prevent duplicate participant
        if (participantRepository
                .existsByGroupChat_GroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("User already in group");
        }

        // 6️⃣ Add user
        Participant participant = Participant.builder()
                .userId(userId)
                .username(newMember.getUsername())
                .role(ParticipantRole.MEMBER)
                .groupChat(group)
                .build();

        group.getParticipants().add(participant);

        return repository.save(group);
    }

    
    @Override
    public GroupChat removeUserFromGroup(Long groupId, Long userId) {
        // Get current user (from JWT)
        UserPrincipal principal = (UserPrincipal)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        Long requesterId = principal.getId();

        GroupChat group = repository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Participant requester = participantRepository
                .findByGroupChat_GroupIdAndUserId(groupId, requesterId)
                .orElseThrow(() ->
                        new AccessDeniedException("You are not a member of this group")
                );

        if (requester.getRole() != ParticipantRole.OWNER &&
            requester.getRole() != ParticipantRole.ADMIN) {
            throw new AccessDeniedException("Only OWNER or ADMIN can remove users");
        }

        if (requesterId.equals(userId)) {
            throw new RuntimeException("You cannot remove yourself from the group");
        }

        // Member to be removed
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }

        Participant participant = participantRepository.findByGroupChat_GroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new EntityNotFoundException("User not present in the group"));

        // If the member to be removed is the owner
        if (participant.getRole().equals(ParticipantRole.OWNER)) {
            throw new RuntimeException("You cannot remove the owner from the group");
        }

        participantRepository.delete(participant);

        group.getParticipants().removeIf(p -> p.getUserId().equals(userId));

        return group;
    }
}
