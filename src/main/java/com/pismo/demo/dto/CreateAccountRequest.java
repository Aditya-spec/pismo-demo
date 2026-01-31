package com.pismo.demo.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank(message = "Document number is required")
        @JsonProperty("document_number")
        String documentNumber
) {}