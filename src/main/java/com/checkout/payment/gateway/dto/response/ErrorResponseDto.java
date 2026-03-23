package com.checkout.payment.gateway.dto.response;

import com.checkout.payment.gateway.exception.FieldErrorDetail;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {
  @Schema(example = "Payment not found", description = "Error message describing the issue")
  private String message;

  @Schema(example = "404", description = "HTTP status code")
  private int status;

  @Schema(description = "Field-level validation errors (if applicable)")
  private List<FieldErrorDetail> errors;

  @Schema(example = "2026-03-22T10:30:45Z", description = "ISO 8601 timestamp when the error occurred")
  private String timestamp;
}
