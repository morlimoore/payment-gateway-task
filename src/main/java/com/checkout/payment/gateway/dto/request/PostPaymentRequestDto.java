package com.checkout.payment.gateway.dto.request;

import com.checkout.payment.gateway.enums.CurrencyCode;
import com.checkout.payment.gateway.validation.ValidEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;
import java.time.YearMonth;

@Data
public class PostPaymentRequestDto implements Serializable {

  @JsonProperty("card_number")
  @NotBlank(message = "Card number is required")
  @Size(min = 14, max = 19, message = "Card number must be between 14 and 19 digits")
  @Pattern(regexp = "\\d+", message = "Card number must contain only digits")
  @Schema(example = "4242424242424242", description = "Card number (14-19 digits)")
  private String cardNumber;

  @JsonProperty("expiry_month")
  @NotNull(message = "Expiry month is required")
  @Min(value = 1, message = "Expiry month must be between 1 and 12")
  @Max(value = 12, message = "Expiry month must be between 1 and 12")
  @Schema(example = "12", description = "Expiry month (1-12)")
  private Integer expiryMonth;

  @JsonProperty("expiry_year")
  @NotNull(message = "Expiry year is required")
  @Schema(example = "2025", description = "Expiry year (must be in the future)")
  private Integer expiryYear;

  @NotBlank(message = "Currency is required")
  @ValidEnum(enumClass = CurrencyCode.class, message = "Unsupported currency.")
  @Schema(example = "USD", description = "ISO 4217 currency code (USD, EUR, GBP)")
  private String currency;

  @NotNull(message = "Amount is required")
  @Min(value = 1, message = "Amount must be greater than 0")
  @Schema(example = "1050", description = "Amount in minor units (e.g., 1050 = $10.50)")
  private Integer amount;

  @NotBlank(message = "CVV is required")
  @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
  @Pattern(regexp = "\\d+", message = "CVV must contain only digits")
  @Schema(example = "123", description = "Card security code (3-4 digits)")
  private String cvv;

  @AssertTrue(message = "Card expiry date must be in the future")
  private boolean isExpiryDate() {
    if (expiryMonth == null || expiryYear == null || expiryMonth < 1 || expiryMonth > 12) {
      return true;
    }
    try {
      return !YearMonth.of(expiryYear, expiryMonth).isBefore(YearMonth.now());
    } catch (Exception e) {
      return false;
    }
  }
}
