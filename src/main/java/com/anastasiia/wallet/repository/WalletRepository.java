package com.anastasiia.wallet.repository;

import java.util.Optional;
import java.util.UUID;

import com.anastasiia.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query(value = "SELECT * FROM wallets WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Wallet> findForUpdateById(UUID id);
}