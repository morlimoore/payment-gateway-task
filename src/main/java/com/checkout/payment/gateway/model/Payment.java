package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class Payment {
  private UUID id;
  private PaymentStatus status;
  private String authorizationCode;
  private int cardNumberLastFour;
  private int expiryMonth;
  private int expiryYear;
  private String currency;
  private int amount;
}
