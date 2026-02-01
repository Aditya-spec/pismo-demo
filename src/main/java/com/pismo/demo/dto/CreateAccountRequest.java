package com.pismo.demo.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank(message = "Document number is required")
        @Size(max = 10, message = "Document number must be at most 10 characters")
        @JsonProperty("document_number")
        String documentNumber
) {}