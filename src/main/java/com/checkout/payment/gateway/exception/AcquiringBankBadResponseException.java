package com.checkout.payment.gateway.exception;

public class AcquiringBankBadResponseException extends RuntimeException {

  public AcquiringBankBadResponseException(String message) {
    super(message);
  }
}
