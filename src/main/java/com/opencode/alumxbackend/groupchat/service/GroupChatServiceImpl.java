package com.opencode.alumxbackend.groupchat.service;

import com.opencode.alumxbackend.groupchat.dto.GroupChatRequest;
import com.opencode.alumxbackend.groupchat.model.GroupChat;
import com.opencode.alumxbackend.groupchat.model.Participant;
import com.opencode.alumxbackend.groupchat.repository.GroupChatRepository;
import lombok.RequiredArgsConstructor;
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
    // private final UserRepository userRepository;

    @Override
    public GroupChat createGroup(GroupChatRequest request) {
        GroupChat group = GroupChat.builder()
                .groupName(request.getName())
                .createdAt(LocalDateTime.now())
                .build();

        // Map DTO participants -> Participant entity
        List<Participant> participants = request.getParticipants().stream()
                .map(p -> {

                    // var existingParticipant = userRepository.findById(p.getUserId())
                    //         .orElseThrow(()-> new RuntimeException(
                    //                 "User with ID " + p.getUserId() + "does not exits "
                    //         ));
                    Participant participant = new Participant();
                    participant.setUserId(p.getUserId());
                    participant.setUsername(p.getUsername());
                    participant.setGroupChat(group);
                    return participant;
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
