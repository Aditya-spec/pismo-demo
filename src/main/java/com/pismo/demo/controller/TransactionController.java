package com.pismo.demo.controller;


import com.pismo.demo.dto.TransactionRequestDTO;
import com.pismo.demo.dto.TransactionResponseDTO;
import com.pismo.demo.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a transaction")
    public ResponseEntity<TransactionResponseDTO> createTransaction(@RequestHeader(value = "key") String idempotencyKey,
                                                                    @RequestBody @Valid TransactionRequestDTO request) {
        TransactionResponseDTO responseDTO = transactionService.createTransaction(request, idempotencyKey);
        return ResponseEntity.ok(responseDTO);
    }
}
