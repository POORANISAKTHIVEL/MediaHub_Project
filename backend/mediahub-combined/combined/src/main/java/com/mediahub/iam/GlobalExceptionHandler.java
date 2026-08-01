package com.mediahub.iam;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadJson(
            HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
            Map.of("status", "error",
                   "message", "Request body is missing or malformed. Ensure 'Content-Type: application/json' and valid JSON."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("message", "Access Denied"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(
            RuntimeException ex) {
        String msg = ex.getMessage();
        int status = switch (msg) {
            // IAM module errors
            case "USER_NOT_FOUND",
                 "ROLE_NOT_FOUND",
                 "PERMISSION_NOT_FOUND",
                 "AUDIT_LOG_NOT_FOUND",
                 // AuditLog module errors
                 "AUDIT_EVENT_NOT_FOUND",
                 "POLICY_NOT_FOUND",
                 "ALERT_NOT_FOUND",
                 "REPORT_NOT_FOUND"          -> 404;
            case "EMAIL_ALREADY_EXISTS",
                 "ROLE_ALREADY_EXISTS",
                 "PERMISSION_ALREADY_EXISTS",
                 "ALREADY_SUSPENDED",
                 "PERMISSION_ALREADY_ASSIGNED",
                 "POLICY_NAME_ALREADY_EXISTS" -> 409;
            case "INVALID_CREDENTIALS",
                 "REFRESH_TOKEN_INVALID"     -> 401;
            case "ACCOUNT_SUSPENDED",
                 "ACCOUNT_INACTIVE",
                 "CANNOT_SUSPEND_SELF"       -> 403;
            case "ALREADY_ACTIVE",
                 "ALERT_ALREADY_RESOLVED"    -> 400;
            default                           -> 500;
        };
        return ResponseEntity.status(status).body(
            Map.of("status", "error", "message", msg));
    }
}
