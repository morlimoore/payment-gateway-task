package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.dto.request.AcquiringBankRequestDto;
import com.checkout.payment.gateway.dto.response.AcquiringBankResponseDto;

public interface AcquiringBankApiClient {

  AcquiringBankResponseDto processPayment(AcquiringBankRequestDto paymentRequest);
}
