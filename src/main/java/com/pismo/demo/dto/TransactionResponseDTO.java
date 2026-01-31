package com.pismo.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
    @JsonProperty("transaction_id")
    Long transactionId,

    @JsonProperty("account_id")
    Long accountId,

    @JsonProperty("operation_type_id")
    Integer operationTypeId,

    @JsonProperty("amount")
    BigDecimal amount,

    @JsonProperty("event_date")
    LocalDateTime eventDate
) {}