package com.anastasiia.wallet.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.anastasiia.wallet.entity.Wallet;
import com.anastasiia.wallet.exception.InsufficientFundsException;
import com.anastasiia.wallet.exception.WalletNotFoundException;
import com.anastasiia.wallet.repository.WalletRepository;
import com.anastasiia.wallet.service.WalletOperationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Юнит тесты сервиса WalletServiceImpl")
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private final UUID walletId = UUID.randomUUID();
    private final BigDecimal amount = new BigDecimal("123.45");

    @Test
    @Order(1)
    @DisplayName("Успешное пополнение кошелька")
    void processOperationDeposit_success() {
        // given
        WalletOperationRequest request = createRequest(walletId, WalletOperationRequest.OperationType.DEPOSIT, amount);
        when(walletRepository.existsById(walletId)).thenReturn(true);

        // when
        walletService.processOperation(request);

        // then
        verify(walletRepository).deposit(walletId, amount);
        verify(walletRepository, never()).withdraw(any(), any());
    }

    @Test
    @Order(2)
    @DisplayName("Успешное списание с кошелька")
    void processOperationWithdraw_success() {
        // given
        WalletOperationRequest request = createRequest(walletId, WalletOperationRequest.OperationType.WITHDRAW, amount);
        when(walletRepository.existsById(walletId)).thenReturn(true);
        when(walletRepository.withdraw(walletId, amount)).thenReturn(1);

        // when
        walletService.processOperation(request);

        // then
        verify(walletRepository).withdraw(walletId, amount);
        verify(walletRepository, never()).deposit(any(), any());
    }

    @Test
    @Order(3)
    @DisplayName("Ошибка при операции с несуществующим кошельком")
    void processOperationWithWalletNotExist_throwsException() {
        // given
        WalletOperationRequest request = createRequest(walletId, WalletOperationRequest.OperationType.DEPOSIT, amount);
        when(walletRepository.existsById(walletId)).thenReturn(false);

        // then
        assertThrows(WalletNotFoundException.class, () -> walletService.processOperation(request));
        verify(walletRepository, never()).deposit(any(), any());
        verify(walletRepository, never()).withdraw(any(), any());
    }

    @Test
    @Order(4)
    @DisplayName("Ошибка при списании суммы больше текущего баланса")
    void processOperationWithInsufficientFunds_throwsException() {
        // given
        WalletOperationRequest request = createRequest(walletId, WalletOperationRequest.OperationType.WITHDRAW, amount);
        when(walletRepository.existsById(walletId)).thenReturn(true);
        when(walletRepository.withdraw(walletId, amount)).thenReturn(0);
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(
                Wallet.builder()
                        .id(walletId)
                        .balance(BigDecimal.ZERO)
                        .build()
        ));

        // then
        assertThrows(InsufficientFundsException.class, () -> walletService.processOperation(request));
        verify(walletRepository).withdraw(walletId, amount);
    }

    @Test
    @Order(5)
    @DisplayName("Создание нового кошелька")
    void createWallet_success() {
        // given
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var result = walletService.createWallet();

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @Order(6)
    @DisplayName("Получение баланса существующего кошелька")
    void getBalance_success() {
        // given
        BigDecimal expectedBalance = new BigDecimal("123.45");
        var wallet = Wallet.builder()
                .id(walletId)
                .balance(expectedBalance)
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // when
        BigDecimal result = walletService.getBalance(walletId);

        // then
        assertThat(result).isEqualTo(expectedBalance);
    }

    @Test
    @Order(7)
    @DisplayName("Ошибка при получении баланса несуществующего кошелька")
    void getBalance_throwsException() {
        // given
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // then
        assertThrows(WalletNotFoundException.class, () -> walletService.getBalance(walletId));
    }

    private WalletOperationRequest createRequest(UUID id, WalletOperationRequest.OperationType type, BigDecimal amount) {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(id);
        request.setOperationType(type);
        request.setAmount(amount);
        return request;
    }
}