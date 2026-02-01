package com.pismo.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pismo.demo.dto.AccountResponseDTO;
import com.pismo.demo.dto.CreateAccountRequest;
import com.pismo.demo.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Create Account - Success (201 Created)")
    void createAccount_Success() throws Exception {
        String docNumber = "1234567890";
        CreateAccountRequest request = new CreateAccountRequest(docNumber);
        AccountResponseDTO responseDTO = new AccountResponseDTO(1L, docNumber);

        when(accountService.createAccount(docNumber)).thenReturn(responseDTO);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_id").value(1))
                .andExpect(jsonPath("$.document_number").value(docNumber));
    }

    @Test
    @DisplayName("Create Account - Validation Fail (Empty Document) -> 400 Bad Request")
    void createAccount_ValidationFail_Empty() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.documentNumber").exists());
    }

    @Test
    @DisplayName("Create Account - Validation Fail (Too Long) -> 400 Bad Request")
    void createAccount_ValidationFail_TooLong() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("123456789012");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.documentNumber").value("Document number must be at most 10 characters"));
    }


    @Test
    @DisplayName("Get Account - Success (200 OK)")
    void getAccount_Success() throws Exception {
        Long accountId = 1L;
        AccountResponseDTO responseDTO = new AccountResponseDTO(accountId, "1234567890");

        when(accountService.getAccount(accountId)).thenReturn(responseDTO);

        mockMvc.perform(get("/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.document_number").value("1234567890"));
    }

    @Test
    @DisplayName("Get Account - Not Found (404)")
    void getAccount_NotFound() throws Exception {
        Long accountId = 99L;
        when(accountService.getAccount(accountId))
                .thenThrow(new EntityNotFoundException("Account not found with ID: " + accountId));

        mockMvc.perform(get("/accounts/{id}", accountId))
                .andExpect(status().isNotFound()) // Expect 404
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("Account not found with ID: 99"));
    }
}