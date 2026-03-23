package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.controller.documentation.PaymentGatewayControllerDocumentation;
import com.checkout.payment.gateway.dto.request.PostPaymentRequestDto;
import com.checkout.payment.gateway.dto.response.PostPaymentResponseDto;
import com.checkout.payment.gateway.dto.response.GetPaymentResponseDto;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentGatewayController implements PaymentGatewayControllerDocumentation {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<GetPaymentResponseDto> getPaymentById(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.OK).body(paymentGatewayService.getPaymentById(id));
  }

  @Override
  @PostMapping
  public ResponseEntity<PostPaymentResponseDto> processPayment(
      @RequestBody @Valid PostPaymentRequestDto paymentRequestDto,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
    return ResponseEntity.status(HttpStatus.CREATED).body(paymentGatewayService.processPayment(paymentRequestDto, idempotencyKey));
  }
}

