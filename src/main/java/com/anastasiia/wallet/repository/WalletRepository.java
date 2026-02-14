package com.anastasiia.wallet.repository;

import java.math.BigDecimal;
import java.util.UUID;

import com.anastasiia.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.id = :walletId")
    void deposit(@Param("walletId") UUID walletId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount WHERE w.id = :walletId AND w.balance >= :amount")
    int withdraw(@Param("walletId") UUID walletId, @Param("amount") BigDecimal amount);
}