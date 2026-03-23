package com.checkout.payment.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;

@Data
public class AcquiringBankResponseDto implements Serializable {

  private boolean authorized;
  @JsonProperty("authorization_code")
  private String authorizationCode;
}
