package com.checkout.payment.gateway.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.checkout.payment.gateway.fixture.PaymentTestFixture;
import com.checkout.payment.gateway.model.Payment;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentRepository")
class PaymentRepositoryTest {

  private final PaymentRepository repository = new InMemoryPaymentRepository();

  @Nested
  @DisplayName("add")
  class AddTests {

    @Test
    @DisplayName("should add payment successfully")
    void shouldAddPaymentSuccessfully() {
      Payment payment = PaymentTestFixture.createAuthorizedPayment();

      repository.add(payment);

      var result = repository.get(payment.getId());
      assertTrue(result.isPresent());
      assertEquals(payment, result.get());
    }

    @Test
    @DisplayName("should overwrite existing payment with same id")
    void shouldOverwriteExistingPaymentWithSameId() {
      UUID paymentId = UUID.randomUUID();
      Payment payment1 = PaymentTestFixture.createAuthorizedPaymentWithId(paymentId);
      Payment payment2 = PaymentTestFixture.createDeclinedPaymentWithId(paymentId);

      repository.add(payment1);
      repository.add(payment2);

      var result = repository.get(paymentId);
      assertTrue(result.isPresent());
      assertEquals(payment2, result.get());
    }

    @Test
    @DisplayName("should add multiple payments")
    void shouldAddMultiplePayments() {
      Payment payment1 = PaymentTestFixture.createAuthorizedPayment();
      Payment payment2 = PaymentTestFixture.createDeclinedPayment();

      repository.add(payment1);
      repository.add(payment2);

      assertTrue(repository.get(payment1.getId()).isPresent());
      assertTrue(repository.get(payment2.getId()).isPresent());
    }
  }

  @Nested
  @DisplayName("get")
  class GetTests {

    @Test
    @DisplayName("should return empty optional when payment does not exist")
    void shouldReturnEmptyOptionalWhenPaymentDoesNotExist() {
      UUID paymentId = UUID.randomUUID();

      var result = repository.get(paymentId);

      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return payment when it exists")
    void shouldReturnPaymentWhenItExists() {
      Payment payment = PaymentTestFixture.createAuthorizedPayment();
      repository.add(payment);

      var result = repository.get(payment.getId());

      assertTrue(result.isPresent());
      assertEquals(payment.getId(), result.get().getId());
    }

    @Test
    @DisplayName("should retrieve correct payment from multiple stored")
    void shouldRetrieveCorrectPaymentFromMultipleStored() {
      Payment payment1 = PaymentTestFixture.createAuthorizedPayment();
      Payment payment2 = PaymentTestFixture.createDeclinedPayment();
      repository.add(payment1);
      repository.add(payment2);

      var result = repository.get(payment1.getId());

      assertTrue(result.isPresent());
      assertEquals(payment1.getId(), result.get().getId());
      assertEquals(payment1.getStatus(), result.get().getStatus());
    }

    @Test
    @DisplayName("should return different payment objects but with same data")
    void shouldReturnDifferentPaymentObjectsButWithSameData() {
      Payment payment = PaymentTestFixture.createAuthorizedPayment();
      repository.add(payment);

      var result1 = repository.get(payment.getId());
      var result2 = repository.get(payment.getId());

      assertTrue(result1.isPresent());
      assertTrue(result2.isPresent());
      assertEquals(result1.get(), result2.get());
    }
  }
}
