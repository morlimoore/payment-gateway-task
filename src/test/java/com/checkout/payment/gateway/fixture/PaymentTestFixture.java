package com.checkout.payment.gateway.fixture;

import com.checkout.payment.gateway.dto.request.PostPaymentRequestDto;
import com.checkout.payment.gateway.dto.response.AcquiringBankResponseDto;
import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.Payment;
import java.util.UUID;

public class PaymentTestFixture {

  private static final String VALID_CARD_NUMBER = "4532015112830366";
  private static final String VALID_CVV = "123";
  private static final Integer VALID_EXPIRY_MONTH = 12;
  private static final Integer VALID_EXPIRY_YEAR = 2026;
  private static final String VALID_CURRENCY = "USD";
  private static final Integer VALID_AMOUNT = 1050;

  public static PostPaymentRequestDto createValidPostPaymentRequestDto() {
    PostPaymentRequestDto request = new PostPaymentRequestDto();
    request.setCardNumber(VALID_CARD_NUMBER);
    request.setCvv(VALID_CVV);
    request.setExpiryMonth(VALID_EXPIRY_MONTH);
    request.setExpiryYear(VALID_EXPIRY_YEAR);
    request.setCurrency(VALID_CURRENCY);
    request.setAmount(VALID_AMOUNT);
    return request;
  }

  public static PostPaymentRequestDto createValidPostPaymentRequestDtoWithCardNumber(String cardNumber) {
    PostPaymentRequestDto request = createValidPostPaymentRequestDto();
    request.setCardNumber(cardNumber);
    return request;
  }

  public static PostPaymentRequestDto createValidPostPaymentRequestDtoWithAmount(Integer amount) {
    PostPaymentRequestDto request = createValidPostPaymentRequestDto();
    request.setAmount(amount);
    return request;
  }

  public static PostPaymentRequestDto createValidPostPaymentRequestDtoWithCurrency(String currency) {
    PostPaymentRequestDto request = createValidPostPaymentRequestDto();
    request.setCurrency(currency);
    return request;
  }

  public static AcquiringBankResponseDto createAuthorizedBankResponse() {
    AcquiringBankResponseDto response = new AcquiringBankResponseDto();
    response.setAuthorized(true);
    response.setAuthorizationCode("AUTH123456");
    return response;
  }

  public static AcquiringBankResponseDto createDeclinedBankResponse() {
    AcquiringBankResponseDto response = new AcquiringBankResponseDto();
    response.setAuthorized(false);
    response.setAuthorizationCode(null);
    return response;
  }

  public static Payment createAuthorizedPayment() {
    Payment payment = new Payment();
    payment.setId(UUID.randomUUID());
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setAuthorizationCode("AUTH123456");
    payment.setCardNumberLastFour(366);
    payment.setExpiryMonth(VALID_EXPIRY_MONTH);
    payment.setExpiryYear(VALID_EXPIRY_YEAR);
    payment.setCurrency(VALID_CURRENCY);
    payment.setAmount(VALID_AMOUNT);
    return payment;
  }

  public static Payment createDeclinedPayment() {
    Payment payment = new Payment();
    payment.setId(UUID.randomUUID());
    payment.setStatus(PaymentStatus.DECLINED);
    payment.setCardNumberLastFour(366);
    payment.setExpiryMonth(VALID_EXPIRY_MONTH);
    payment.setExpiryYear(VALID_EXPIRY_YEAR);
    payment.setCurrency(VALID_CURRENCY);
    payment.setAmount(VALID_AMOUNT);
    return payment;
  }

  public static Payment createAuthorizedPaymentWithId(UUID id) {
    Payment payment = createAuthorizedPayment();
    payment.setId(id);
    return payment;
  }

  public static Payment createDeclinedPaymentWithId(UUID id) {
    Payment payment = createDeclinedPayment();
    payment.setId(id);
    return payment;
  }

  public static PostPaymentResponseDto createResponseFromPayment(Payment payment) {
    PostPaymentResponseDto response = new PostPaymentResponseDto();
    response.setId(payment.getId());
    response.setStatus(payment.getStatus());
    response.setCardNumberLastFour(payment.getCardNumberLastFour());
    response.setExpiryMonth(payment.getExpiryMonth());
    response.setExpiryYear(payment.getExpiryYear());
    response.setCurrency(payment.getCurrency());
    response.setAmount(payment.getAmount());
    return response;
  }

  public static String getValidCardNumber() {
    return VALID_CARD_NUMBER;
  }

  public static int getValidCardLastFour() {
    return 366;
  }
}
