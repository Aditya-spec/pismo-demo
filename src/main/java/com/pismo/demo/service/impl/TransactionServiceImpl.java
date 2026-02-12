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
import java.util.Optional;

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
     * Processes a financial transaction for a specific account with idempotency and balance checks.
     * <p>
     * This method performs the following steps:
     * 1. <b>Idempotency Check:</b> Checks if a transaction with the provided {@code idempotencyKey} already exists.
     * If found, returns the existing transaction to prevent duplicate processing.
     * 2. <b>Validation:</b> Verifies that the Account and Operation Type exist.
     * 3. <b>Business Logic:</b>
     * <ul>
     * <li><b>Debits (Negative Multiplier):</b> Checks if the transaction amount is within the allowed limit
     * (Current Balance + 1000.00 buffer). If valid, subtracts the amount from the balance.</li>
     * <li><b>Credits (Positive Multiplier):</b> Adds the transaction amount to the current balance.</li>
     * </ul>
     * 4. <b>Persistence:</b> Saves the new transaction record and updates the account balance in the database.
     *
     * @param request        The DTO containing account ID, operation type, and transaction amount.
     * @param idempotencyKey A unique key (header) provided by the client to ensure the request is processed only once.
     * @return TransactionResponseDTO containing the persisted transaction details.
     * @throws EntityNotFoundException  if the account does not exist.
     * @throws IllegalArgumentException if the operation type is invalid or if the transaction exceeds the available limit.
     */
    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request, String idempotencyKey) {
        log.info("Initiating transaction. Account: {}, Type: {}, Amount: {}, key:{}",
                request.accountId(), request.operationTypeId(), request.amount(), idempotencyKey);

        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            Transaction t = existingTransaction.get();
            log.info("Idempotency hit! Returning existing transaction ID: {}", t.getId());
            return new TransactionResponseDTO(
                    t.getId(),
                    t.getAccount().getId(),
                    t.getOperationTypeId(),
                    t.getAmount(),
                    t.getEventDate()
            );
        }

        try {
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

            BigDecimal  currentBalance = account.getBalance();
            if(type.getSignMultiplier() < 0){
               BigDecimal newLimit = currentBalance.add(BigDecimal.valueOf(1000));
                if(newLimit.compareTo(request.amount()) < 0){
                    throw new IllegalArgumentException("Transaction denied: limit exceeded");
                }else{
                    account.setBalance(currentBalance.subtract(request.amount()));
                }
            }
            else {
                account.setBalance(currentBalance.add(request.amount()));
            }
            Transaction transaction = new Transaction();
            transaction.setAccount(account);
            transaction.setOperationTypeId(type.getId().intValue());
            transaction.setAmount(finalAmount);
            transaction.setEventDate(LocalDateTime.now());
            transaction.setIdempotencyKey(idempotencyKey);
            Transaction savedTransaction = transactionRepository.save(transaction);
            accountRepository.save(account);
            log.info("Transaction saved successfully with ID: {}", savedTransaction.getId());
            return new TransactionResponseDTO(
                    savedTransaction.getId(),
                    savedTransaction.getAccount().getId(),
                    savedTransaction.getOperationTypeId(),
                    savedTransaction.getAmount(),
                    savedTransaction.getEventDate()
            );

        } catch (Exception e) {
            log.error("FAILED to create transaction. Account: {} :: error {}", request.accountId(), e.getMessage());
            throw e;
        }
    }
}