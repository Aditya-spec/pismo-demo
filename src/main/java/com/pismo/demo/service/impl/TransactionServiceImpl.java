package com.pismo.demo.service.impl;

import com.pismo.demo.dto.TransactionRequestDTO;
import com.pismo.demo.dto.TransactionResponseDTO;
import com.pismo.demo.entity.*;
import com.pismo.demo.repository.*;
import com.pismo.demo.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OperationTypeRepository operationTypeRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository,
                                  OperationTypeRepository operationTypeRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.operationTypeRepository = operationTypeRepository;
    }

    /**
     * Creates a new transaction for a specific account.
     * <p>
     * This method performs the following steps:
     * 1. Validates the existence of the Account.
     * 2. Validates the existence of the Operation Type.
     * 3. Calculates the final amount by applying the Operation Type's sign multiplier
     * (e.g., converts positive input to negative for withdrawals).
     * 4. Persists the transaction with a timestamp.
     *
     * @param request The DTO containing account ID, operation type, and amount.
     * @return TransactionResponseDTO containing the persisted transaction details.
     * @throws EntityNotFoundException if the account does not exist.
     * @throws IllegalArgumentException if the operation type is invalid.
     */
    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        log.info("Initiating transaction. Account: {}, Type: {}, Amount: {}",
                request.accountId(), request.operationTypeId(), request.amount());

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> {
                    log.error("Transaction failed: Account ID {} does not exists", request.accountId());
                    return new EntityNotFoundException("Account not found");
                });

        OperationType type = operationTypeRepository.findById(Long.valueOf(request.operationTypeId()))
                .orElseThrow(() -> {
                    log.error("Transaction failed: Invalid Operation Type ID {}", request.operationTypeId());
                    return new IllegalArgumentException("Invalid Operation Type ID");
                });

        BigDecimal finalAmount = request.amount().abs().multiply(BigDecimal.valueOf(type.getSignMultiplier()));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setOperationTypeId(type.getId().intValue());
        transaction.setAmount(finalAmount);
        transaction.setEventDate(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction saved successfully with ID: {}", savedTransaction.getId());

        return new TransactionResponseDTO(
                savedTransaction.getId(),
                savedTransaction.getAccount().getId(),
                savedTransaction.getOperationTypeId(),
                savedTransaction.getAmount(),
                savedTransaction.getEventDate()
        );
    }
}