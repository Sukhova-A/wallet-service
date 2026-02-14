package com.anastasiia.wallet.controller;

import java.math.BigDecimal;
import java.util.UUID;

import com.anastasiia.wallet.entity.Wallet;

public record WalletResponse(UUID walletId, BigDecimal balance) {
    public static WalletResponse fromEntity(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }
}