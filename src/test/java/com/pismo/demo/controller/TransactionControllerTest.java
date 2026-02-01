package com.pismo.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pismo.demo.dto.TransactionRequestDTO;
import com.pismo.demo.dto.TransactionResponseDTO;
import com.pismo.demo.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Create Transaction - Success (200 OK)")
    void createTransaction_Success() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO(1L, 1, new BigDecimal("100.00"));
        
        TransactionResponseDTO response = new TransactionResponseDTO(
                555L, 
                1L, 
                1, 
                new BigDecimal("-100.00"), 
                LocalDateTime.now()
        );

        when(transactionService.createTransaction(any(TransactionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction_id").value(555))
                .andExpect(jsonPath("$.account_id").value(1))
                .andExpect(jsonPath("$.amount").value(-100.00));
    }

    @Test
    @DisplayName("Create Transaction - Account Not Found -> 404 Not Found")
    void createTransaction_AccountNotFound() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO(99L, 1, BigDecimal.TEN);

        when(transactionService.createTransaction(any()))
                .thenThrow(new EntityNotFoundException("Account not found"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("Account not found"));
    }

    @Test
    @DisplayName("Create Transaction - Invalid Operation Type -> 400 Bad Request")
    void createTransaction_InvalidOperationType() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO(1L, 99, BigDecimal.TEN);

        when(transactionService.createTransaction(any()))
                .thenThrow(new IllegalArgumentException("Invalid Operation Type ID"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid Operation Type ID"));
    }

    @Test
    @DisplayName("Create Transaction - Validation Error (Empty Body) -> 400 Bad Request")
    void createTransaction_ValidationFail() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) 
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }
}