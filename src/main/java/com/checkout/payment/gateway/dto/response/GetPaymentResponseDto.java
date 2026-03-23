package com.checkout.payment.gateway.dto.response;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.UUID;

@Data
public class GetPaymentResponseDto {
  @Schema(example = "123e4567-e89b-12d3-a456-426614174000", description = "Unique payment identifier")
  private UUID id;

  @Schema(example = "Authorized", description = "Payment status: Authorized or Declined")
  private PaymentStatus status;

  @Schema(example = "4242", description = "Last four digits of the card number")
  private int cardNumberLastFour;

  @Schema(example = "12", description = "Card expiry month")
  private int expiryMonth;

  @Schema(example = "2026", description = "Card expiry year")
  private int expiryYear;

  @Schema(example = "USD", description = "ISO 4217 currency code")
  private String currency;

  @Schema(example = "1050", description = "Amount in minor units (e.g., 1050 = $10.50)")
  private int amount;

  public static GetPaymentResponseDto fromPayment(Payment payment) {
    GetPaymentResponseDto dto = new GetPaymentResponseDto();
    dto.setId(payment.getId());
    dto.setStatus(payment.getStatus());
    dto.setCardNumberLastFour(payment.getCardNumberLastFour());
    dto.setExpiryMonth(payment.getExpiryMonth());
    dto.setExpiryYear(payment.getExpiryYear());
    dto.setCurrency(payment.getCurrency());
    dto.setAmount(payment.getAmount());
    return dto;
  }
}
