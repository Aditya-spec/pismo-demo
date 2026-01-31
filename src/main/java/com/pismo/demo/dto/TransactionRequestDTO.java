package com.pismo.demo.dto;



import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionRequestDTO(
        @NotNull(message = "Account ID is required")
        @JsonProperty("account_id")
        Long accountId,

        @NotNull(message = "Operation Type ID is required")
        @JsonProperty("operation_type_id")
        Integer operationTypeId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {}
