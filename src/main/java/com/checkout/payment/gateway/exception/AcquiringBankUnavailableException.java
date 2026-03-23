package com.checkout.payment.gateway.exception;

public class AcquiringBankUnavailableException extends RuntimeException {

  public AcquiringBankUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
