package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.*;

import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import com.checkout.payment.gateway.fixture.PaymentTestFixture;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("IdempotencyService")
class IdempotencyServiceTest {

  private final IdempotencyService service = new InMemoryIdempotencyService();

  @Nested
  @DisplayName("getCachedResponse")
  class GetCachedResponseTests {

    @Test
    @DisplayName("should return empty optional when cache is empty")
    void shouldReturnEmptyOptionalWhenCacheIsEmpty() {
      String idempotencyKey = UUID.randomUUID().toString();

      var result = service.getCachedResponse(idempotencyKey);

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty optional when idempotency key is null")
    void shouldReturnEmptyOptionalWhenIdempotencyKeyIsNull() {
      var result = service.getCachedResponse(null);

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty optional when idempotency key is blank")
    void shouldReturnEmptyOptionalWhenIdempotencyKeyIsBlank() {
      var result = service.getCachedResponse("   ");

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return cached response when key matches")
    void shouldReturnCachedResponseWhenKeyMatches() {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentResponseDto cachedResponse = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createAuthorizedPayment()
      );
      service.cacheResponse(idempotencyKey, cachedResponse);

      var result = service.getCachedResponse(idempotencyKey);

      assertTrue(result.isPresent());
      assertEquals(cachedResponse.getId(), result.get().getId());
    }

    @Test
    @DisplayName("should return empty optional when key does not match")
    void shouldReturnEmptyOptionalWhenKeyDoesNotMatch() {
      String key1 = UUID.randomUUID().toString();
      String key2 = UUID.randomUUID().toString();
      PostPaymentResponseDto response = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createAuthorizedPayment()
      );
      service.cacheResponse(key1, response);

      var result = service.getCachedResponse(key2);

      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("cacheResponse")
  class CacheResponseTests {

    @Test
    @DisplayName("should cache response with valid idempotency key")
    void shouldCacheResponseWithValidIdempotencyKey() {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentResponseDto response = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createAuthorizedPayment()
      );

      service.cacheResponse(idempotencyKey, response);

      var cachedResult = service.getCachedResponse(idempotencyKey);
      assertTrue(cachedResult.isPresent());
      assertEquals(response.getId(), cachedResult.get().getId());
    }

    @Test
    @DisplayName("should not cache when idempotency key is null")
    void shouldNotCacheWhenIdempotencyKeyIsNull() {
      PostPaymentResponseDto response = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createAuthorizedPayment()
      );

      service.cacheResponse(null, response);

      var result = service.getCachedResponse(null);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should not cache when idempotency key is blank")
    void shouldNotCacheWhenIdempotencyKeyIsBlank() {
      PostPaymentResponseDto response = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createAuthorizedPayment()
      );

      service.cacheResponse("   ", response);

      var result = service.getCachedResponse("   ");
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should not cache when response is null")
    void shouldNotCacheWhenResponseIsNull() {

      String idempotencyKey = UUID.randomUUID().toString();

      service.cacheResponse(idempotencyKey, null);

      var result = service.getCachedResponse(idempotencyKey);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should overwrite previous cache entry with same key")
    void shouldOverwritePreviousCacheEntryWithSameKey() {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentResponseDto response1 = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createAuthorizedPayment()
      );
      PostPaymentResponseDto response2 = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createDeclinedPayment()
      );

      service.cacheResponse(idempotencyKey, response1);
      service.cacheResponse(idempotencyKey, response2);

      var cachedResult = service.getCachedResponse(idempotencyKey);
      assertTrue(cachedResult.isPresent());
      assertEquals(response2.getId(), cachedResult.get().getId());
    }

    @Test
    @DisplayName("should maintain multiple cache entries with different keys")
    void shouldMaintainMultipleCacheEntriesWithDifferentKeys() {
      String key1 = UUID.randomUUID().toString();
      String key2 = UUID.randomUUID().toString();
      PostPaymentResponseDto response1 = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createAuthorizedPayment()
      );
      PostPaymentResponseDto response2 = PaymentTestFixture.createResponseFromPayment(
          PaymentTestFixture.createDeclinedPayment()
      );

      service.cacheResponse(key1, response1);
      service.cacheResponse(key2, response2);

      var result1 = service.getCachedResponse(key1);
      var result2 = service.getCachedResponse(key2);
      assertTrue(result1.isPresent());
      assertTrue(result2.isPresent());
      assertEquals(response1.getId(), result1.get().getId());
      assertEquals(response2.getId(), result2.get().getId());
    }
  }
}
