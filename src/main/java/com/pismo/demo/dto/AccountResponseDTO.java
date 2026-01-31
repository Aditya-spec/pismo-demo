package com.pismo.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountResponseDTO(
        @JsonProperty("account_id")
        Long accountId,

        @JsonProperty("document_number")
        String documentNumber
) {}