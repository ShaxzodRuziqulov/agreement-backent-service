package com.example.agreement.exeption;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ------------- 404 -------------
    @ExceptionHandler({
            UserNotFoundException.class,
            ContractNotFoundException.class,
            ChangeSetPersister.NotFoundException.class,
            EntityNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(Exception ex, HttpServletRequest req) {
        return build(ex, req, HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // ------------- 409 -------------
    @ExceptionHandler({UserAlreadyExistsException.class, ConflictException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(Exception ex, HttpServletRequest req) {
        return build(ex, req, HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // ------------- 400 -------------
    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class, RentalException.class,
            InvalidOtpException.class, OtpExpiredException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        return build(ex, req, HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // ------------- 429 -------------
    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ApiErrorResponse> handleTooMany(TooManyAttemptsException ex, HttpServletRequest req) {
        return build(ex, req, HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), null);
    }

    // ------------- 403 -------------
    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ApiErrorResponse> handleBlocked(UserBlockedException ex, HttpServletRequest req) {
        return build(ex, req, HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return build(ex, req, HttpStatus.UNAUTHORIZED, "Authentication failed", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(ex, req, HttpStatus.FORBIDDEN, "Access denied", null);
    }

    // ------------- 400 (Bean Validation @Valid) -------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);

        return build(ex, req, HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    // ------------- 409 (DB constraint, unique, check, fk...) -------------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest req) {

        Map<String, Object> details = new LinkedHashMap<>();

        Throwable root = rootCause(ex);

        // Hibernate constraint exception (often contains constraintName)
        ConstraintViolationException hibernateCve = findCause(ex, ConstraintViolationException.class);
        if (hibernateCve != null) {
            details.put("constraint", hibernateCve.getConstraintName());
            details.put("sqlState", safeSqlState(hibernateCve));
        }

        // Postgres often puts useful text into root message
        details.put("rootCause", root.getClass().getSimpleName());
        details.put("rootMessage", safeMessage(root));

        // user-friendly message
        String msg = "Database constraint violated";
        if (details.get("constraint") != null) {
            msg += ": " + details.get("constraint");
        }

        // Check constraint -> 23514. Unique -> 23505. FK -> 23503 (Postgres).
        String sqlState = (String) details.get("sqlState");
        if ("23514".equals(sqlState)) {
            msg = "Invalid value violates database CHECK constraint"
                    + (details.get("constraint") != null ? (": " + details.get("constraint")) : "");
        }

        return build(ex, req, HttpStatus.CONFLICT, msg, details);
    }

    // ------------- 500 -------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        String traceId = ensureTraceId(req);
        log.error("Unexpected error traceId={}", traceId, ex);

        return build(ex, req, HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error occurred",
                Map.of("hint", "Check server logs with traceId"));
    }

    // ---------------- helpers ----------------

    private ResponseEntity<ApiErrorResponse> build(Exception ex,
                                                   HttpServletRequest req,
                                                   HttpStatus status,
                                                   String message,
                                                   Map<String, Object> details) {

        String traceId = ensureTraceId(req);

        // 4xx lar uchun stack trace spam qilmaymiz, lekin root sababni log qilamiz
        if (status.is4xxClientError()) {
            log.warn("Handled {} traceId={} path={} msg={}",
                    ex.getClass().getSimpleName(), traceId, req.getRequestURI(), safeMessage(ex));
        }

        ApiErrorResponse body = ApiErrorResponse.builder()
                .message(message)
                .error(status.getReasonPhrase())
                .status(status.value())
                .type(ex.getClass().getSimpleName())
                .path(req.getRequestURI())
                .traceId(traceId)
                .timestamp(OffsetDateTime.now())
                .details(details)
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }

    private static String safeMessage(Throwable t) {
        return t == null ? null : String.valueOf(t.getMessage());
    }

    private static <T extends Throwable> T findCause(Throwable ex, Class<T> type) {
        Throwable cur = ex;
        while (cur != null) {
            if (type.isInstance(cur)) return type.cast(cur);
            cur = cur.getCause();
        }
        return null;
    }

    private static String safeSqlState(ConstraintViolationException cve) {
        try {
            return cve.getSQLException() != null ? cve.getSQLException().getSQLState() : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String ensureTraceId(HttpServletRequest req) {
        Object existing = req.getAttribute("traceId");
        if (existing != null) return existing.toString();
        String traceId = UUID.randomUUID().toString();
        req.setAttribute("traceId", traceId);
        return traceId;
    }
}
