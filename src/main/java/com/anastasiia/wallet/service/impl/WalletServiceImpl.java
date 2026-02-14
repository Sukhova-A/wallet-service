package com.anastasiia.wallet.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import com.anastasiia.wallet.entity.Wallet;
import com.anastasiia.wallet.exception.InsufficientFundsException;
import com.anastasiia.wallet.exception.WalletNotFoundException;
import com.anastasiia.wallet.repository.WalletRepository;
import com.anastasiia.wallet.service.WalletOperationRequest;
import com.anastasiia.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    public Wallet createWallet() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(walletId)
                .balance(BigDecimal.ZERO)
                .build();
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void processOperation(WalletOperationRequest request) {
        validateAmount(request.getAmount());

        if (!walletRepository.existsById(request.getWalletId())) {
            throw new WalletNotFoundException(request.getWalletId());
        }

        switch (request.getOperationType()) {
            case DEPOSIT -> walletRepository.deposit(request.getWalletId(), request.getAmount());
            case WITHDRAW -> {
                int updatedRows = walletRepository.withdraw(request.getWalletId(), request.getAmount());
                if (updatedRows == 0) {
                    BigDecimal currentBalance = getBalance(request.getWalletId());
                    throw new InsufficientFundsException(request.getWalletId(), request.getAmount(), currentBalance);
                }
            }
            default -> throw new IllegalArgumentException("Unknown operation type: " + request.getOperationType());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
    }
}