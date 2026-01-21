package com.opencode.alumxbackend.groupchatmessages.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserNotMemberException extends RuntimeException {
    public UserNotMemberException(Long userId) {
        super("User is not a member of this group: " + userId);
    }
}

