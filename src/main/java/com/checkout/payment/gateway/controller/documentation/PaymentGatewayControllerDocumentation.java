package com.checkout.payment.gateway.controller.documentation;

import com.checkout.payment.gateway.dto.request.PostPaymentRequestDto;
import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import com.checkout.payment.gateway.dto.response.GetPaymentResponseDto;
import com.checkout.payment.gateway.dto.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Payment Gateway", description = "APIs for processing and retrieving payment information")
public interface PaymentGatewayControllerDocumentation {

  @GetMapping("/{id}")
  @Operation(summary = "Retrieve payment details", description = "Retrieves the details of a previously processed payment by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Payment found and returned successfully",
          content = @Content(schema = @Schema(implementation = GetPaymentResponseDto.class))),
      @ApiResponse(responseCode = "404", description = "Payment not found",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  ResponseEntity<GetPaymentResponseDto> getPaymentById(@PathVariable UUID id);

  @PostMapping
  @Operation(summary = "Process a payment", description = "Submits a payment for processing by the acquiring bank")
  @RequestBody(content = @Content(schema = @Schema(implementation = PostPaymentRequestDto.class)))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Payment processed successfully (Authorized or Declined)",
          content = @Content(schema = @Schema(implementation = PostPaymentResponseDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid payment request (Rejected)",
          content = @Content(schema = @Schema(implementation = PostPaymentResponseDto.class))),
      @ApiResponse(responseCode = "503", description = "Acquiring bank temporarily unavailable",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
      @ApiResponse(responseCode = "500", description = "Internal server error",
          content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
  })
  ResponseEntity<PostPaymentResponseDto> processPayment(
      @RequestBody @Valid PostPaymentRequestDto paymentRequestDto,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey);
}
