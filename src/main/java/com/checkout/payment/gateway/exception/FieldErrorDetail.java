package com.checkout.payment.gateway.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldErrorDetail {
  @Schema(example = "cardNumber", description = "Name of the field that failed validation")
  private String field;

  @Schema(example = "123", description = "The value that was rejected")
  private Object rejectedValue;

  @Schema(example = "Card number must be between 14 and 19 digits", description = "Validation error message")
  private String message;
}
