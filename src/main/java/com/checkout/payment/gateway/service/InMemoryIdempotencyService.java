package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InMemoryIdempotencyService implements IdempotencyService {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryIdempotencyService.class);
  private final Map<String, PostPaymentResponseDto> cache = new ConcurrentHashMap<>();

  @Override
  public Optional<PostPaymentResponseDto> getCachedResponse(String idempotencyKey) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      return Optional.empty();
    }
    Optional<PostPaymentResponseDto> cachedResponse = Optional.ofNullable(cache.get(idempotencyKey));
    if (cachedResponse.isPresent()) {
      LOG.debug("Idempotency cache hit for key: {}", idempotencyKey);
    } else {
      LOG.debug("Idempotency cache miss for key: {}", idempotencyKey);
    }
    return cachedResponse;
  }

  @Override
  public void cacheResponse(String idempotencyKey, PostPaymentResponseDto response) {
    if (idempotencyKey == null || idempotencyKey.isBlank() || response == null) {
      return;
    }
    cache.put(idempotencyKey, response);
    LOG.debug("Cached payment response for idempotency key: {}", idempotencyKey);
  }
}
