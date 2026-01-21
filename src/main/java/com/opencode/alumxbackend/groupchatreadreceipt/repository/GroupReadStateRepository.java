package com.opencode.alumxbackend.groupchatreadreceipt.repository;

import com.opencode.alumxbackend.groupchatreadreceipt.model.GroupReadState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupReadStateRepository extends JpaRepository<GroupReadState,Long> {
    Optional<GroupReadState> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupReadState> findByGroupId(Long groupId);

}

