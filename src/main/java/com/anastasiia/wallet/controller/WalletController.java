package com.anastasiia.wallet.controller;

import java.math.BigDecimal;
import java.util.UUID;

import com.anastasiia.wallet.entity.Wallet;
import com.anastasiia.wallet.service.WalletOperationRequest;
import com.anastasiia.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet() {
        Wallet wallet = walletService.createWallet();
        return ResponseEntity.ok(WalletResponse.fromEntity(wallet));
    }

    @PostMapping("/wallet")
    public ResponseEntity<Void> processOperation(@Valid @RequestBody WalletOperationRequest request) {
        log.debug("Processing operation: {}", request);

        walletService.processOperation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID walletId) {
        log.debug("Getting balance for wallet: {}", walletId);

        BigDecimal balance = walletService.getBalance(walletId);
        return ResponseEntity.ok(balance);
    }
}