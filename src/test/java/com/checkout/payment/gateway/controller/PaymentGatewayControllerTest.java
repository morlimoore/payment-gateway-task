package com.checkout.payment.gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.checkout.payment.gateway.client.AcquiringBankApiClient;
import com.checkout.payment.gateway.dto.request.PostPaymentRequestDto;
import com.checkout.payment.gateway.fixture.PaymentTestFixture;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PaymentGatewayController")
class PaymentGatewayControllerTest {

  private static final String URL = "/payments";

  @Autowired
  private MockMvc mvc;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AcquiringBankApiClient bankApiClient;

  private PostPaymentRequestDto validRequest;

  @BeforeEach
  void setUp() {
    validRequest = PaymentTestFixture.createValidPostPaymentRequestDto();
    when(bankApiClient.processPayment(any())).thenReturn(
        PaymentTestFixture.createAuthorizedBankResponse()
    );
  }

  @Nested
  @DisplayName("GET /payments/{id}")
  class GetPaymentByIdTests {

    @Test
    @DisplayName("should return 200 with correct payment details when payment exists")
    void shouldReturnPaymentWhenItExists() throws Exception {
      Payment payment = PaymentTestFixture.createAuthorizedPayment();
      paymentRepository.add(payment);

      mvc.perform(get(URL + "/" + payment.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(payment.getId().toString()))
          .andExpect(jsonPath("$.status").value("Authorized"))
          .andExpect(jsonPath("$.cardNumberLastFour").value(PaymentTestFixture.getValidCardLastFour()))
          .andExpect(jsonPath("$.expiryMonth").value(12))
          .andExpect(jsonPath("$.expiryYear").value(2026))
          .andExpect(jsonPath("$.currency").value("USD"))
          .andExpect(jsonPath("$.amount").value(1050));
    }

    @Test
    @DisplayName("should return 404 when payment does not exist")
    void shouldReturn404WhenPaymentDoesNotExist() throws Exception {
      UUID nonExistentId = UUID.randomUUID();

      mvc.perform(get(URL + "/" + nonExistentId))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("should return declined payment status")
    void shouldReturnDeclinedPaymentStatus() throws Exception {
      Payment payment = PaymentTestFixture.createDeclinedPayment();
      paymentRepository.add(payment);

      mvc.perform(get(URL + "/" + payment.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("Declined"));
    }
  }

  @Nested
  @DisplayName("POST /payments")
  class ProcessPaymentTests {

    @Test
    @DisplayName("should return 201 for valid payment request")
    void shouldReturn201ForValidPayment() throws Exception {
      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").isNotEmpty())
          .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("should return REJECTED status for missing card number")
    void shouldReturnRejectedForMissingCardNumber() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      request.setCardNumber(null);

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    @DisplayName("should return REJECTED status for invalid card length")
    void shouldReturnRejectedForInvalidCardLength() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDtoWithCardNumber("123");

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    @DisplayName("should return REJECTED status for non-numeric card number")
    void shouldReturnRejectedForNonNumericCardNumber() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDtoWithCardNumber("abcd1234567890xy");

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    @DisplayName("should return REJECTED status for invalid expiry month")
    void shouldReturnRejectedForInvalidExpiryMonth() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      request.setExpiryMonth(13);

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    @DisplayName("should return REJECTED status for expired card")
    void shouldReturnRejectedForExpiredCard() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      request.setExpiryYear(2020);

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    @DisplayName("should return REJECTED status for invalid CVV length")
    void shouldReturnRejectedForInvalidCvvLength() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      request.setCvv("12");

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    @DisplayName("should return REJECTED status for unsupported currency")
    void shouldReturnRejectedForUnsupportedCurrency() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDtoWithCurrency("XYZ");

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    @DisplayName("should return REJECTED status for zero amount")
    void shouldReturnRejectedForZeroAmount() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDtoWithAmount(0);

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    @DisplayName("should include validation errors in response for rejected payment")
    void shouldIncludeValidationErrorsInResponse() throws Exception {
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      request.setCardNumber(null);

      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors").isArray())
          .andExpect(jsonPath("$.errors[0].field").exists())
          .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    @DisplayName("should mask card number in successful response")
    void shouldMaskCardNumberInResponse() throws Exception {
      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.cardNumberLastFour").value(PaymentTestFixture.getValidCardLastFour()));
    }

    @Test
    @DisplayName("should return complete response structure with all required fields")
    void shouldReturnCompleteResponseStructure() throws Exception {
      mvc.perform(post(URL)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").exists())
          .andExpect(jsonPath("$.status").exists())
          .andExpect(jsonPath("$.cardNumberLastFour").exists())
          .andExpect(jsonPath("$.expiryMonth").exists())
          .andExpect(jsonPath("$.expiryYear").exists())
          .andExpect(jsonPath("$.currency").exists())
          .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    @DisplayName("should support idempotency - same request returns same payment ID")
    void shouldSupportIdempotency() throws Exception {
      String idempotencyKey = UUID.randomUUID().toString();
      PostPaymentRequestDto request = PaymentTestFixture.createValidPostPaymentRequestDto();
      String requestBody = objectMapper.writeValueAsString(request);

      var response1 = mvc.perform(post(URL)
          .header("Idempotency-Key", idempotencyKey)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isCreated())
          .andReturn();

      String body1 = response1.getResponse().getContentAsString();
      String paymentId1 = objectMapper.readTree(body1).get("id").asText();

      var response2 = mvc.perform(post(URL)
          .header("Idempotency-Key", idempotencyKey)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isCreated())
          .andReturn();

      String body2 = response2.getResponse().getContentAsString();
      String paymentId2 = objectMapper.readTree(body2).get("id").asText();

      assertEquals(paymentId1, paymentId2, "Idempotent requests should return same payment ID");
    }
  }
}

