package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.dto.request.AcquiringBankRequestDto;
import com.checkout.payment.gateway.dto.response.AcquiringBankResponseDto;
import com.checkout.payment.gateway.exception.AcquiringBankUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class BankClientSimulatorApiClient implements AcquiringBankApiClient {

  private final RestTemplate restTemplate;
  private final String bankSimulatorUrl;

  public BankClientSimulatorApiClient(RestTemplate restTemplate,  @Value("${bank.simulator.url}") String bankSimulatorUrl) {
    this.restTemplate = restTemplate;
    this.bankSimulatorUrl = bankSimulatorUrl;
  }

  private static final Logger LOG = LoggerFactory.getLogger(BankClientSimulatorApiClient.class);

  @Override
  public AcquiringBankResponseDto processPayment(AcquiringBankRequestDto paymentRequest) {
    String cardNumberLastFour = paymentRequest.getCardNumber()
        .substring(paymentRequest.getCardNumber().length() - 4);
    LOG.debug("Sending payment request to acquiring bank: URL={}, cardNumber=****{}",
        bankSimulatorUrl, cardNumberLastFour);
    
    try {
      HttpEntity<AcquiringBankRequestDto> request = new HttpEntity<>(paymentRequest);
      ResponseEntity<AcquiringBankResponseDto> response = restTemplate.exchange(
          bankSimulatorUrl,
          HttpMethod.POST,
          request,
          AcquiringBankResponseDto.class
      );
      
      LOG.debug("Payment request processed successfully, response status: {}", response.getStatusCode());
      return response.getBody();
    } catch (HttpServerErrorException.ServiceUnavailable ex) {
      LOG.warn("Acquiring bank service unavailable (503): {}", ex.getMessage());
      throw new AcquiringBankUnavailableException("Service temporarily unavailable, please try again later", ex);
    } catch (HttpServerErrorException ex) {
      LOG.error("Acquiring bank returned error status {}: {}", ex.getStatusCode(), ex.getMessage());
      throw new AcquiringBankUnavailableException("Bank service error, please try again later", ex);
    } catch (ResourceAccessException ex) {
      LOG.error("Network/connection error communicating with acquiring bank: {}", ex.getMessage());
      throw new AcquiringBankUnavailableException("Unable to reach bank service, please try again later", ex);
    } catch (Exception ex) {
      LOG.error("Unexpected error during bank communication: {}", ex.getMessage(), ex);
      throw new AcquiringBankUnavailableException("Something went wrong, please try again later", ex);
    }
  }
}
