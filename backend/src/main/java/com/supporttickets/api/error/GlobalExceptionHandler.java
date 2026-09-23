package com.supporttickets.api.error;

import com.supporttickets.domain.IllegalTransitionException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
        .map(this::toDetail)
        .collect(Collectors.toList());
    return envelope(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details);
  }

  @ExceptionHandler(RequestValidationException.class)
  public ResponseEntity<ErrorResponse> handleRequestValidation(RequestValidationException ex) {
    return envelope(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), ex.getDetails());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
    return envelope(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Request body is invalid or malformed",
        List.of());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String field = ex.getName() == null ? "parameter" : ex.getName();
    return envelope(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Request validation failed",
        List.of(new ErrorDetail(field, "Invalid value")));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    return envelope(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Request validation failed",
        List.of());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return envelope(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), List.of());
  }

  @ExceptionHandler(IllegalTransitionException.class)
  public ResponseEntity<ErrorResponse> handleIllegalTransition(IllegalTransitionException ex) {
    return envelope(HttpStatus.CONFLICT, "ILLEGAL_TRANSITION", ex.getMessage(), List.of());
  }

  @ExceptionHandler(CommentNotAllowedException.class)
  public ResponseEntity<ErrorResponse> handleCommentNotAllowed(CommentNotAllowedException ex) {
    return envelope(HttpStatus.CONFLICT, "COMMENT_NOT_ALLOWED", ex.getMessage(), List.of());
  }

  private ErrorDetail toDetail(FieldError fieldError) {
    return new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
  }

  private ResponseEntity<ErrorResponse> envelope(
      HttpStatus status,
      String error,
      String message,
      List<ErrorDetail> details) {
    ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), error, message, details);
    return ResponseEntity.status(status).body(body);
  }
}
