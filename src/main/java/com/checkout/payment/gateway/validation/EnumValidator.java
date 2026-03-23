package com.checkout.payment.gateway.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

  private Class<? extends Enum<?>> enumClass;
  private String message;

  @Override
  public void initialize(ValidEnum annotation) {
    this.enumClass = annotation.enumClass();
    this.message = annotation.message();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return true;

    boolean isValid = Arrays.stream(enumClass.getEnumConstants())
        .anyMatch(e -> e.name().equals(value));

    if (!isValid) {
      String allowedValues = String.join(", ",
          Arrays.stream(enumClass.getEnumConstants())
              .map(Enum::name)
              .toArray(String[]::new));

      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
          message + " Allowed values: " + allowedValues
      ).addConstraintViolation();
    }

    return isValid;
  }
}
