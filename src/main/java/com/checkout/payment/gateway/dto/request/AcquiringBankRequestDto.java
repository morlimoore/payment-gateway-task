package com.checkout.payment.gateway.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class AcquiringBankRequestDto implements Serializable {

  @JsonProperty("card_number")
  private String cardNumber;
  @JsonProperty("expiry_date")
  private String expiryDate;
  private String currency;
  private int amount;
  private String cvv;

  public static AcquiringBankRequestDto fromPostPaymentRequestDto(PostPaymentRequestDto dto) {
    return AcquiringBankRequestDto.builder()
        .cardNumber(dto.getCardNumber())
        .currency(dto.getCurrency())
        .amount(dto.getAmount())
        .cvv(dto.getCvv())
        .expiryDate(String.format("%d/%d", dto.getExpiryMonth(), dto.getExpiryYear()))
        .build();
  }
}
