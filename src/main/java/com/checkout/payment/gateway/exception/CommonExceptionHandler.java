package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.dto.response.ErrorResponseDto;
import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import com.checkout.payment.gateway.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.Instant;
import java.util.List;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<PostPaymentResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
    LOG.debug("Payment request rejected");
    List<FieldErrorDetail> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(this::mapToFieldError)
        .toList();

    PostPaymentResponseDto response = new PostPaymentResponseDto();
    response.setStatus(PaymentStatus.REJECTED);
    response.setErrors(errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handlePaymentNotFoundException(PaymentNotFoundException ex) {
    LOG.debug("Payment not found: {}", ex.getMessage());
    return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AcquiringBankUnavailableException.class)
  public ResponseEntity<ErrorResponseDto> handleAcquiringBankUnavailableException(AcquiringBankUnavailableException ex) {
    LOG.error("Acquiring bank unavailable (503): {}", ex.getMessage(), ex);
    return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(AcquiringBankBadResponseException.class)
  public ResponseEntity<ErrorResponseDto> handleAcquiringBankBadResponseException(AcquiringBankBadResponseException ex) {
    LOG.error("Received bad response from Acquiring bank: {}", ex.getMessage(), ex);
    return buildErrorResponse(ex, HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleUnexpectedException(Exception ex) {
    LOG.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
    return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<ErrorResponseDto> buildErrorResponse(Exception ex, HttpStatus status) {
    ErrorResponseDto error = ErrorResponseDto.builder()
        .message(ex.getMessage())
        .status(status.value())
        .timestamp(Instant.now().toString())
        .build();
    return ResponseEntity.status(status).body(error);
  }

  private FieldErrorDetail mapToFieldError(FieldError error) {
    return FieldErrorDetail.builder()
        .field(error.getField())
        .rejectedValue(error.getRejectedValue())
        .message(error.getDefaultMessage())
        .build();
  }
}
