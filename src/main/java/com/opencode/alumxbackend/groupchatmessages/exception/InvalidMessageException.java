package com.opencode.alumxbackend.groupchatmessages.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(String msg) {
        super(msg);
    }
}
