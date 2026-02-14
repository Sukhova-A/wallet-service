package com.anastasiia.wallet.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.anastasiia.wallet.service.WalletOperationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Интеграционные тесты WalletController")
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test_wallet_db");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @Order(1)
    @DisplayName("Создание нового кошелька")
    void createWallet_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").exists())
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @Order(2)
    @DisplayName("Пополнение существующего кошелька")
    void depositOperation_success() throws Exception {
        // given
        UUID walletId = createWallet();

        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(WalletOperationRequest.OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("555.55"));

        // when & then
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(content().string("555.55"));
    }

    @Test
    @Order(3)
    @DisplayName("Списание с баланса существующего кошелька")
    void withdrawOperation_success() throws Exception {
        // given
        UUID walletId = createWallet();
        deposit(walletId, "555.55");

        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(WalletOperationRequest.OperationType.WITHDRAW);
        request.setAmount(new BigDecimal("222.22"));

        // when & then
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(content().string("333.33"));
    }

    @Test
    @Order(4)
    @DisplayName("Списание при недостаточном балансе - ошибка")
    void withdrawOperation_InsufficientFunds() throws Exception {
        // given
        UUID walletId = createWallet();
        deposit(walletId, "111.11");

        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(WalletOperationRequest.OperationType.WITHDRAW);
        request.setAmount(new BigDecimal("555.55"));

        // when & then
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(5)
    @DisplayName("Операция с несуществующим кошельком - ошибка")
    void depositOperation_notFound() throws Exception {
        // given
        UUID walletId = UUID.randomUUID();

        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(WalletOperationRequest.OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("111.11"));

        // when & then
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(6)
    @DisplayName("Получение баланса несуществующего кошелька - ошибка")
    void getBalance_notFound() throws Exception {
        // given
        UUID walletId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(7)
    @DisplayName("Операция с некорректным JSON - ошибка")
    void operationWithWrongJson_badRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("{random}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(8)
    @DisplayName("Операция с отрицательной суммой - ошибка")
    void operationWithNegativeAmount_badRequest() throws Exception {
        // given
        UUID walletId = createWallet();

        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(WalletOperationRequest.OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("-11.11"));

        // when & then
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private UUID createWallet() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/create"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        WalletResponse walletResponse = objectMapper.readValue(response, WalletResponse.class);
        return walletResponse.walletId();
    }

    private void deposit(UUID walletId, String amount) throws Exception {
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(WalletOperationRequest.OperationType.DEPOSIT);
        request.setAmount(new BigDecimal(amount));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}