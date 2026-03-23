package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import java.util.Optional;

public interface IdempotencyService {

  Optional<PostPaymentResponseDto> getCachedResponse(String idempotencyKey);
  void cacheResponse(String idempotencyKey, PostPaymentResponseDto response);
}
