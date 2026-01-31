package com.pismo.demo.service;

import com.pismo.demo.dto.AccountResponseDTO;
import com.pismo.demo.entity.Account;

public interface AccountService {
    AccountResponseDTO createAccount(String documentNumber);
    AccountResponseDTO getAccount(Long id);
}
