package by.bsuir.growpathserver.trainee.infrastructure.web;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.domain.exception.DuplicateInternshipProgramTitleException;
import by.bsuir.growpathserver.trainee.domain.exception.InternshipProgramLockedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> notFound(NoSuchElementException ex, HttpServletRequest request) {
        log.warn("NoSuchElementException: path='{}', message='{}'", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("IllegalArgumentException: path='{}', message='{}'", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Void> invalidId(NumberFormatException ex, HttpServletRequest request) {
        log.warn("NumberFormatException: path='{}', message='{}'", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> responseStatus(ResponseStatusException ex, HttpServletRequest request) {
        String reason = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        log.warn("Request failed: status={}, path='{}', reason='{}'",
                 ex.getStatusCode().value(), request.getRequestURI(), reason);
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                "status", ex.getStatusCode().value(),
                "error", reason,
                "path", request.getRequestURI()));
    }

    @ExceptionHandler(DuplicateInternshipProgramTitleException.class)
    public ResponseEntity<Void> duplicateInternshipProgramTitle() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(InternshipProgramLockedException.class)
    public ResponseEntity<Void> internshipProgramLocked() {
        return ResponseEntity.badRequest().build();
    }
}
