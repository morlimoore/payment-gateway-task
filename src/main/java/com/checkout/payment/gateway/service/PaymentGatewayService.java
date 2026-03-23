package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.AcquiringBankApiClient;
import com.checkout.payment.gateway.dto.request.PostPaymentRequestDto;
import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import com.checkout.payment.gateway.dto.response.GetPaymentResponseDto;
import com.checkout.payment.gateway.dto.request.AcquiringBankRequestDto;
import com.checkout.payment.gateway.dto.response.AcquiringBankResponseDto;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.AcquiringBankBadResponseException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.repository.PaymentRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);
  private static final int CARD_LAST_FOUR_LENGTH = 4;

  private final PaymentRepository paymentRepository;
  private final AcquiringBankApiClient bankApiClient;
  private final IdempotencyService idempotencyService;

  public PaymentGatewayService(
      PaymentRepository paymentRepository,
      AcquiringBankApiClient bankApiClient,
      IdempotencyService idempotencyService
  ) {
    this.paymentRepository = paymentRepository;
    this.bankApiClient = bankApiClient;
    this.idempotencyService = idempotencyService;
  }

  public GetPaymentResponseDto getPaymentById(UUID id) {
    LOG.debug("Retrieving payment with ID: {}", id);
    Payment payment = paymentRepository.get(id)
        .orElseThrow(() -> new PaymentNotFoundException(String.format("Payment with ID: '%s' not found", id)));
    return GetPaymentResponseDto.fromPayment(payment);
  }

  public PostPaymentResponseDto processPayment(PostPaymentRequestDto paymentRequestDto, String idempotencyKey) {
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      var cachedResponse = idempotencyService.getCachedResponse(idempotencyKey);
      if (cachedResponse.isPresent()) {
        LOG.info("Idempotent request - returning cached response for key: {}", idempotencyKey);
        return cachedResponse.get();
      }
    }

    LOG.debug("Processing payment for card ending in {}", getLastFourDigits(paymentRequestDto.getCardNumber()));
    
    AcquiringBankRequestDto bankRequest = AcquiringBankRequestDto.fromPostPaymentRequestDto(paymentRequestDto);
    AcquiringBankResponseDto bankResponse = bankApiClient.processPayment(bankRequest);
    
    if (bankResponse == null) {
      LOG.error("Received null response from acquiring bank");
      throw new AcquiringBankBadResponseException("Invalid response from bank");
    }
    
    UUID paymentId = UUID.randomUUID();
    Payment payment = buildPayment(paymentId, paymentRequestDto, bankResponse);
    paymentRepository.add(payment);
    LOG.debug("Payment stored in repository with ID: {}, Status: {}", paymentId, payment.getStatus());
    
    PostPaymentResponseDto paymentResponseDto = PostPaymentResponseDto.fromPayment(payment);
    
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      idempotencyService.cacheResponse(idempotencyKey, paymentResponseDto);
    }
    
    LOG.info("Payment {} processed with status: {}", payment.getId(), payment.getStatus());
    return paymentResponseDto;
  }

  private Payment buildPayment(
      UUID paymentId,
      PostPaymentRequestDto requestDto,
      AcquiringBankResponseDto bankResponse) {
    
    Payment payment = new Payment();
    payment.setId(paymentId);
    payment.setStatus(bankResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED);
    payment.setAuthorizationCode(bankResponse.getAuthorizationCode());
    payment.setCardNumberLastFour(getLastFourDigits(requestDto.getCardNumber()));
    payment.setExpiryMonth(requestDto.getExpiryMonth());
    payment.setExpiryYear(requestDto.getExpiryYear());
    payment.setCurrency(requestDto.getCurrency());
    payment.setAmount(requestDto.getAmount());
    
    return payment;
  }

  private int getLastFourDigits(String cardNumber) {
    String lastFour = cardNumber.substring(cardNumber.length() - CARD_LAST_FOUR_LENGTH);
    return Integer.parseInt(lastFour);
  }
}
