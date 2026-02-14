package com.anastasiia.wallet.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID walletId, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format(
                "Insufficient funds in wallet with id %s: requested %s, available %s",
                walletId, requestedAmount, availableBalance
        ));
    }
}