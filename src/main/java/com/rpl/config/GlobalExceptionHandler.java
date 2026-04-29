package com.rpl.config;

import com.rpl.exception.ConflictException;
import com.rpl.exception.IllegalStateTransitionException;
import com.rpl.exception.NotFoundException;
import com.rpl.exception.ValidationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler({ConflictException.class, IllegalStateTransitionException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(RuntimeException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(RuntimeException exception) {
        return Map.of("error", exception.getMessage());
    }
}
