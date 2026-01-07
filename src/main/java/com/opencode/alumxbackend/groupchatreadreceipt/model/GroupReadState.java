package com.opencode.alumxbackend.groupchatreadreceipt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.aot.AbstractAotProcessor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_read_states")
@Builder
public class GroupReadState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;
    private Long userId;
    private Long lastReadMessageId;




}
