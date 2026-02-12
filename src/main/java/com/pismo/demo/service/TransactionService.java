package com.pismo.demo.service;


import com.pismo.demo.dto.TransactionRequestDTO;
import com.pismo.demo.dto.TransactionResponseDTO;
import com.pismo.demo.entity.Transaction;

public interface TransactionService {
    TransactionResponseDTO createTransaction(TransactionRequestDTO request, String idempotencyKey);
}
