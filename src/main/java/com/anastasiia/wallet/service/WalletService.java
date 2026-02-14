package com.anastasiia.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.anastasiia.wallet.entity.Wallet;

public interface WalletService {

    /**
     * Создание нового кошелька
     *
     * @return дто кошелька
     */
    Wallet createWallet();

    /**
     * Выполнение операции с кошельком (пополнение/списание)
     *
     * @param request запрос
     */
    void processOperation(WalletOperationRequest request);

    /**
     * Получение текущего баланса кошелька
     *
     * @param walletId идентификатор кошелька
     * @return текущий баланс кошелька
     */
    BigDecimal getBalance(UUID walletId);
}