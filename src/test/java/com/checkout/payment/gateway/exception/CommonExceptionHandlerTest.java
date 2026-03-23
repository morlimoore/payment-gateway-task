package com.checkout.payment.gateway.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.checkout.payment.gateway.dto.response.ErrorResponseDto;
import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import com.checkout.payment.gateway.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@DisplayName("CommonExceptionHandler")
class CommonExceptionHandlerTest {

  private CommonExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new CommonExceptionHandler();
  }

  @Nested
  @DisplayName("handleValidationException")
  class ValidationExceptionHandlerTests {

    private MethodArgumentNotValidException validationException;

    @BeforeEach
    void setUp() {
      validationException = createValidationException();
    }

    @Test
    @DisplayName("should return 400 status for validation exception")
    void shouldReturn400StatusForValidationException() {
      ResponseEntity<PostPaymentResponseDto> response = handler.handleValidationException(validationException);
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("should return REJECTED status in response")
    void shouldReturnRejectedStatus() {
      ResponseEntity<PostPaymentResponseDto> response = handler.handleValidationException(validationException);
      assertNotNull(response.getBody());
      assertEquals(PaymentStatus.REJECTED, response.getBody().getStatus());
    }

    @Test
    @DisplayName("should include field errors in response")
    void shouldIncludeFieldErrors() {
      ResponseEntity<PostPaymentResponseDto> response = handler.handleValidationException(validationException);
      assertNotNull(response.getBody());
      assertNotNull(response.getBody().getErrors());
      assertFalse(response.getBody().getErrors().isEmpty());
    }

    private MethodArgumentNotValidException createValidationException() {
      BindingResult bindingResult = mock(BindingResult.class);
      FieldError fieldError = new FieldError("request", "cardNumber", "invalid", false, null, null,
          "Card number is required");
      when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

      MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
      when(ex.getBindingResult()).thenReturn(bindingResult);
      return ex;
    }
  }

  @Nested
  @DisplayName("handlePaymentNotFoundException")
  class PaymentNotFoundExceptionHandlerTests {

    private PaymentNotFoundException paymentNotFoundException;

    @BeforeEach
    void setUp() {
      paymentNotFoundException = new PaymentNotFoundException("Payment not found");
    }

    @Test
    @DisplayName("should return 404 status for payment not found exception")
    void shouldReturn404Status() {
      ResponseEntity<ErrorResponseDto> response = handler.handlePaymentNotFoundException(paymentNotFoundException);
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("should include error message in response")
    void shouldIncludeErrorMessage() {
      ResponseEntity<ErrorResponseDto> response = handler.handlePaymentNotFoundException(paymentNotFoundException);
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().contains("not found"));
    }

    @Test
    @DisplayName("should include status code in response")
    void shouldIncludeStatusCode() {
      ResponseEntity<ErrorResponseDto> response = handler.handlePaymentNotFoundException(paymentNotFoundException);
      assertNotNull(response.getBody());
      assertEquals(404, response.getBody().getStatus());
    }

    @Test
    @DisplayName("should include timestamp in response")
    void shouldIncludeTimestamp() {
      ResponseEntity<ErrorResponseDto> response = handler.handlePaymentNotFoundException(paymentNotFoundException);
      assertNotNull(response.getBody());
      assertNotNull(response.getBody().getTimestamp());
    }
  }

  @Nested
  @DisplayName("handleAcquiringBankUnavailableException")
  class AcquiringBankUnavailableExceptionHandlerTests {

    private AcquiringBankUnavailableException bankException;

    @BeforeEach
    void setUp() {
      bankException = new AcquiringBankUnavailableException("Bank unavailable", new RuntimeException("Connection failed"));
    }

    @Test
    @DisplayName("should return 503 status for bank unavailable exception")
    void shouldReturn503Status() {
      ResponseEntity<ErrorResponseDto> response = handler.handleAcquiringBankUnavailableException(bankException);
      assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @DisplayName("should include error message in response")
    void shouldIncludeErrorMessage() {
      ResponseEntity<ErrorResponseDto> response = handler.handleAcquiringBankUnavailableException(bankException);
      assertNotNull(response.getBody());
      assertNotNull(response.getBody().getMessage());
    }

    @Test
    @DisplayName("should include correct status code")
    void shouldIncludeCorrectStatusCode() {
      ResponseEntity<ErrorResponseDto> response = handler.handleAcquiringBankUnavailableException(bankException);
      assertNotNull(response.getBody());
      assertEquals(503, response.getBody().getStatus());
    }
  }

  @Nested
  @DisplayName("handleAcquiringBankBadResponseException")
  class AcquiringBankBadResponseExceptionHandlerTests {

    private AcquiringBankBadResponseException acquiringBankBadResponseException;

    @BeforeEach
    void setUp() {
      acquiringBankBadResponseException = new AcquiringBankBadResponseException("Invalid response from bank");
    }

    @Test
    @DisplayName("should return 502 status for bad response exception")
    void shouldReturn502Status() {
      ResponseEntity<ErrorResponseDto> response = handler.handleAcquiringBankBadResponseException(
          acquiringBankBadResponseException);
      assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
    }

    @Test
    @DisplayName("should include error message in response")
    void shouldIncludeErrorMessage() {
      ResponseEntity<ErrorResponseDto> response = handler.handleAcquiringBankBadResponseException(
          acquiringBankBadResponseException);
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().contains("Invalid response from bank"));
    }

    @Test
    @DisplayName("should include correct status code")
    void shouldIncludeCorrectStatusCode() {
      ResponseEntity<ErrorResponseDto> response = handler.handleAcquiringBankBadResponseException(
          acquiringBankBadResponseException);
      assertNotNull(response.getBody());
      assertEquals(502, response.getBody().getStatus());
    }
  }

  @Nested
  @DisplayName("handleUnexpectedException")
  class UnexpectedExceptionHandlerTests {

    private Exception genericException;

    @BeforeEach
    void setUp() {
      genericException = new Exception("Generic error");
    }

    @Test
    @DisplayName("should return 500 status for generic exception")
    void shouldReturn500Status() {
      ResponseEntity<ErrorResponseDto> response = handler.handleUnexpectedException(genericException);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("should include error message in response")
    void shouldIncludeErrorMessage() {
      ResponseEntity<ErrorResponseDto> response = handler.handleUnexpectedException(genericException);
      assertNotNull(response.getBody());
      assertTrue(response.getBody().getMessage().contains("Generic error"));
    }

    @Test
    @DisplayName("should include correct status code")
    void shouldIncludeCorrectStatusCode() {
      ResponseEntity<ErrorResponseDto> response = handler.handleUnexpectedException(genericException);
      assertNotNull(response.getBody());
      assertEquals(500, response.getBody().getStatus());
    }
  }
}
