package com.opencode.alumxbackend.groupchatmessages.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(String groupId) {
        super("Group not found: " + groupId);
    }
}
