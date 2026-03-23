package com.checkout.payment.gateway.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.checkout.payment.gateway.dto.request.AcquiringBankRequestDto;
import com.checkout.payment.gateway.dto.response.AcquiringBankResponseDto;
import com.checkout.payment.gateway.exception.AcquiringBankUnavailableException;
import com.checkout.payment.gateway.fixture.PaymentTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcquiringBankApiClient")
class AcquiringBankApiClientTest {

  private static final String BANK_URL = "http://bank-simulator.local/api/payment";

  @Mock
  private RestTemplate restTemplate;

  private AcquiringBankApiClient client;

  @BeforeEach
  void setUp() {
    client = new BankClientSimulatorApiClient(restTemplate, BANK_URL);
  }

  @Nested
  @DisplayName("processPayment - Success Cases")
  class SuccessTests {

    @Test
    @DisplayName("should return authorized response from bank")
    void shouldReturnAuthorizedResponse() {
      AcquiringBankRequestDto request = createValidBankRequest();
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createAuthorizedBankResponse();
      ResponseEntity<AcquiringBankResponseDto> responseEntity = ResponseEntity.ok(bankResponse);
      
      when(restTemplate.exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST), 
          any(HttpEntity.class),
          eq(AcquiringBankResponseDto.class)
      )).thenReturn(responseEntity);

      AcquiringBankResponseDto result = client.processPayment(request);

      assertNotNull(result);
      assertTrue(result.isAuthorized());
    }

    @Test
    @DisplayName("should return declined response from bank")
    void shouldReturnDeclinedResponse() {
      AcquiringBankRequestDto request = createValidBankRequest();
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createDeclinedBankResponse();
      ResponseEntity<AcquiringBankResponseDto> responseEntity = ResponseEntity.ok(bankResponse);
      
      when(restTemplate.exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST), 
          any(HttpEntity.class),
          eq(AcquiringBankResponseDto.class)
      )).thenReturn(responseEntity);

      AcquiringBankResponseDto result = client.processPayment(request);

      assertNotNull(result);
      assertFalse(result.isAuthorized());
    }

    @Test
    @DisplayName("should make HTTP POST request to bank API")
    void shouldMakeHttpPostRequest() {
      AcquiringBankRequestDto request = createValidBankRequest();
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createAuthorizedBankResponse();
      ResponseEntity<AcquiringBankResponseDto> responseEntity = ResponseEntity.ok(bankResponse);
      
      when(restTemplate.exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST), 
          any(HttpEntity.class),
          eq(AcquiringBankResponseDto.class)
      )).thenReturn(responseEntity);

      client.processPayment(request);

      verify(restTemplate).exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST),
          any(HttpEntity.class), 
          eq(AcquiringBankResponseDto.class)
      );
    }
  }

  @Nested
  @DisplayName("processPayment - Error Cases")
  class ErrorTests {

    @Test
    @DisplayName("should throw AcquiringBankUnavailableException on 503 response")
    void shouldThrowOnServiceUnavailable() {
      AcquiringBankRequestDto request = createValidBankRequest();
      when(restTemplate.exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST), 
          any(HttpEntity.class),
          eq(AcquiringBankResponseDto.class)
      )).thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

      assertThrows(AcquiringBankUnavailableException.class, () -> client.processPayment(request));
    }

    @Test
    @DisplayName("should throw AcquiringBankUnavailableException on 5xx error")
    void shouldThrowOnServerError() {
      AcquiringBankRequestDto request = createValidBankRequest();
      when(restTemplate.exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST), 
          any(HttpEntity.class),
          eq(AcquiringBankResponseDto.class)
      )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

      assertThrows(AcquiringBankUnavailableException.class, () -> client.processPayment(request));
    }

    @Test
    @DisplayName("should throw AcquiringBankUnavailableException on network error")
    void shouldThrowOnNetworkError() {
      AcquiringBankRequestDto request = createValidBankRequest();
      when(restTemplate.exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST), 
          any(HttpEntity.class),
          eq(AcquiringBankResponseDto.class)
      )).thenThrow(new ResourceAccessException("Network error"));

      assertThrows(AcquiringBankUnavailableException.class, () -> client.processPayment(request));
    }

    @Test
    @DisplayName("should throw AcquiringBankUnavailableException on unexpected error")
    void shouldThrowOnUnexpectedError() {
      AcquiringBankRequestDto request = createValidBankRequest();
      when(restTemplate.exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST), 
          any(HttpEntity.class),
          eq(AcquiringBankResponseDto.class)
      )).thenThrow(new RuntimeException("Unexpected error"));

      assertThrows(AcquiringBankUnavailableException.class, () -> client.processPayment(request));
    }

    @Test
    @DisplayName("should wrap original exception as cause")
    void shouldPreserveCauseException() {
      AcquiringBankRequestDto request = createValidBankRequest();
      HttpServerErrorException cause = new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
      when(restTemplate.exchange(
          eq(BANK_URL), 
          eq(HttpMethod.POST), 
          any(HttpEntity.class),
          eq(AcquiringBankResponseDto.class)
      )).thenThrow(cause);

      AcquiringBankUnavailableException exception = assertThrows(
          AcquiringBankUnavailableException.class,
          () -> client.processPayment(request)
      );
      assertEquals(cause, exception.getCause());
    }
  }

  private AcquiringBankRequestDto createValidBankRequest() {
    return AcquiringBankRequestDto.builder()
        .cardNumber(PaymentTestFixture.getValidCardNumber())
        .expiryDate("12/2026")
        .currency("USD")
        .amount(1050)
        .cvv("123")
        .build();
  }
}
