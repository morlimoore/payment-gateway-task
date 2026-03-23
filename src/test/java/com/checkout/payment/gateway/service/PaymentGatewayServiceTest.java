package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.checkout.payment.gateway.client.AcquiringBankApiClient;
import com.checkout.payment.gateway.dto.request.AcquiringBankRequestDto;
import com.checkout.payment.gateway.dto.request.PostPaymentRequestDto;
import com.checkout.payment.gateway.dto.response.AcquiringBankResponseDto;
import com.checkout.payment.gateway.dto.response.GetPaymentResponseDto;
import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.AcquiringBankBadResponseException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.fixture.PaymentTestFixture;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.repository.PaymentRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentGatewayService")
class PaymentGatewayServiceTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private AcquiringBankApiClient bankApiClient;

  @Mock
  private IdempotencyService idempotencyService;

  @InjectMocks
  private PaymentGatewayService service;

  @Nested
  @DisplayName("getPaymentById")
  class GetPaymentByIdTests {

    @Test
    @DisplayName("should return payment when payment exists")
    void shouldReturnPaymentWhenPaymentExists() {
      UUID paymentId = UUID.randomUUID();
      Payment expectedPayment = PaymentTestFixture.createAuthorizedPaymentWithId(paymentId);
      when(paymentRepository.get(paymentId)).thenReturn(Optional.of(expectedPayment));

      GetPaymentResponseDto result = service.getPaymentById(paymentId);

      assertNotNull(result);
      assertEquals(paymentId, result.getId());
      assertEquals(PaymentStatus.AUTHORIZED, result.getStatus());
      verify(paymentRepository).get(paymentId);
    }

    @Test
    @DisplayName("should throw PaymentNotFoundException when payment does not exist")
    void shouldThrowPaymentNotFoundExceptionWhenPaymentDoesNotExist() {
      UUID paymentId = UUID.randomUUID();
      when(paymentRepository.get(paymentId)).thenReturn(Optional.empty());

      assertThrows(PaymentNotFoundException.class, () -> service.getPaymentById(paymentId));
      verify(paymentRepository).get(paymentId);
    }

    @Test
    @DisplayName("should return correct payment details from repository")
    void shouldReturnCorrectPaymentDetailsFromRepository() {
      UUID paymentId = UUID.randomUUID();
      Payment payment = PaymentTestFixture.createDeclinedPaymentWithId(paymentId);
      when(paymentRepository.get(paymentId)).thenReturn(Optional.of(payment));

      GetPaymentResponseDto result = service.getPaymentById(paymentId);

      assertEquals(PaymentStatus.DECLINED, result.getStatus());
      assertEquals(payment.getCardNumberLastFour(), result.getCardNumberLastFour());
      assertEquals(payment.getExpiryMonth(), result.getExpiryMonth());
      assertEquals(payment.getExpiryYear(), result.getExpiryYear());
      assertEquals(payment.getCurrency(), result.getCurrency());
      assertEquals(payment.getAmount(), result.getAmount());
    }
  }

  @Nested
  @DisplayName("processPayment")
  class ProcessPaymentTests {

    @Test
    @DisplayName("should process authorized payment successfully")
    void shouldProcessAuthorizedPaymentSuccessfully() {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createAuthorizedBankResponse();
      when(bankApiClient.processPayment(any(AcquiringBankRequestDto.class))).thenReturn(bankResponse);

      PostPaymentResponseDto result = service.processPayment(request, null);

      assertNotNull(result);
      assertEquals(PaymentStatus.AUTHORIZED, result.getStatus());
      verify(bankApiClient).processPayment(any(AcquiringBankRequestDto.class));
      verify(paymentRepository).add(any(Payment.class));
    }

    @Test
    @DisplayName("should process declined payment successfully")
    void shouldProcessDeclinedPaymentSuccessfully() {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createDeclinedBankResponse();
      when(bankApiClient.processPayment(any(AcquiringBankRequestDto.class))).thenReturn(bankResponse);

      PostPaymentResponseDto result = service.processPayment(request, null);

      assertNotNull(result);
      assertEquals(PaymentStatus.DECLINED, result.getStatus());
      verify(bankApiClient).processPayment(any(AcquiringBankRequestDto.class));
      verify(paymentRepository).add(any(Payment.class));
    }

    @Test
    @DisplayName("should return cached response when idempotency key matches")
    void shouldReturnCachedResponseWhenIdempotencyKeyMatches() {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      PostPaymentResponseDto cachedResponse = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createAuthorizedPayment()
      );
      when(idempotencyService.getCachedResponse(idempotencyKey))
          .thenReturn(Optional.of(cachedResponse));

      PostPaymentResponseDto result = service.processPayment(request, idempotencyKey);

      assertEquals(cachedResponse.getId(), result.getId());
      assertEquals(cachedResponse.getStatus(), result.getStatus());
      verify(bankApiClient, never()).processPayment(any());
      verify(paymentRepository, never()).add(any());
    }

    @Test
    @DisplayName("should cache response with idempotency key")
    void shouldCacheResponseWithIdempotencyKey() {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createAuthorizedBankResponse();
      when(idempotencyService.getCachedResponse(idempotencyKey)).thenReturn(Optional.empty());
      when(bankApiClient.processPayment(any(AcquiringBankRequestDto.class))).thenReturn(bankResponse);

      service.processPayment(request, idempotencyKey);

      verify(idempotencyService).cacheResponse(eq(idempotencyKey), any(PostPaymentResponseDto.class));
    }

    @Test
    @DisplayName("should not cache response without idempotency key")
    void shouldNotCacheResponseWithoutIdempotencyKey() {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createAuthorizedBankResponse();
      when(bankApiClient.processPayment(any(AcquiringBankRequestDto.class))).thenReturn(bankResponse);

      service.processPayment(request, null);

      verify(idempotencyService, never()).cacheResponse(any(), any());
    }

    @Test
    @DisplayName("should throw AcquiringBankUnavailableException when bank returns null response")
    void shouldThrowAcquiringBankUnavailableExceptionWhenBankReturnsNull() {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      when(idempotencyService.getCachedResponse(anyString())).thenReturn(Optional.empty());
      when(bankApiClient.processPayment(any(AcquiringBankRequestDto.class))).thenReturn(null);

      AcquiringBankBadResponseException exception = assertThrows(
          AcquiringBankBadResponseException.class, () -> service.processPayment(request, idempotencyKey)
      );
      assertTrue(exception.getMessage().contains("Invalid response"));
    }

    @Test
    @DisplayName("should store payment in repository")
    void shouldStorePaymentInRepository() {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createAuthorizedBankResponse();
      when(idempotencyService.getCachedResponse(anyString())).thenReturn(Optional.empty());
      when(bankApiClient.processPayment(any(AcquiringBankRequestDto.class))).thenReturn(bankResponse);

      service.processPayment(request, idempotencyKey);

      verify(paymentRepository).add(argThat(payment ->
          payment.getId() != null &&
          payment.getStatus() == PaymentStatus.AUTHORIZED
      ));
    }

    @Test
    @DisplayName("should set correct payment details from request and bank response")
    void shouldSetCorrectPaymentDetailsFromRequestAndBankResponse() {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDtoWithAmount(5000);
      AcquiringBankResponseDto bankResponse = PaymentTestFixture.createAuthorizedBankResponse();
      when(idempotencyService.getCachedResponse(anyString())).thenReturn(Optional.empty());
      when(bankApiClient.processPayment(any(AcquiringBankRequestDto.class))).thenReturn(bankResponse);

      service.processPayment(request, idempotencyKey);

      verify(paymentRepository).add(argThat(payment ->
          payment.getExpiryMonth() == request.getExpiryMonth() &&
          payment.getExpiryYear() == request.getExpiryYear() &&
          payment.getCurrency().equals(request.getCurrency()) &&
          payment.getAmount() == request.getAmount()
      ));
    }
  }
}
