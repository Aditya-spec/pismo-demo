package com.pismo.demo.controller;

import com.pismo.demo.dto.AccountResponseDTO;
import com.pismo.demo.dto.CreateAccountRequest;
import com.pismo.demo.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountResponseDTO> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        AccountResponseDTO createdAccountDTO = accountService.createAccount(request.documentNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccountDTO);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountResponseDTO> getAccount(@PathVariable Long accountId) {
        AccountResponseDTO accountResponseDTO = accountService.getAccount(accountId);
        return ResponseEntity.ok(accountResponseDTO);
    }
}