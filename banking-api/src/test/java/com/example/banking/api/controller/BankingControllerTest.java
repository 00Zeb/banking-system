package com.example.banking.api.controller;

import com.example.banking.api.dto.LoginRequest;
import com.example.banking.api.dto.RegisterRequest;
import com.example.banking.api.dto.TransactionRequest;
import com.example.banking.api.service.BankingService;
import com.example.banking.user.User;
import com.example.banking.domain.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankingController.class)
@DisplayName("Banking Controller Tests")
class BankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankingService bankingService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private Account mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = new Account();
        mockUser = new User("testuser", "password");
        // Note: We can't easily mock the User constructor, so we'll work with what we have
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest("newuser", "password123");
            when(bankingService.registerUser("newuser", "password123")).thenReturn(true);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User registered successfully"));

            verify(bankingService).registerUser("newuser", "password123");
        }

        @Test
        @DisplayName("Should return conflict when username exists")
        void shouldReturnConflictWhenUsernameExists() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest("existinguser", "password123");
            when(bankingService.registerUser("existinguser", "password123")).thenReturn(false);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Username already exists"));
        }

        @Test
        @DisplayName("Should return bad request for invalid input")
        void shouldReturnBadRequestForInvalidInput() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest("", ""); // Invalid input

            // When & Then
            mockMvc.perform(post("/api/v1/banking/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {

        @Test
        @DisplayName("Should authenticate user successfully")
        void shouldAuthenticateUserSuccessfully() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("testuser", "password");
            when(bankingService.authenticateUser("testuser", "password")).thenReturn(mockUser);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"));

            verify(bankingService).authenticateUser("testuser", "password");
        }

        @Test
        @DisplayName("Should return unauthorized for invalid credentials")
        void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            when(bankingService.authenticateUser("testuser", "wrongpassword")).thenReturn(null);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("Should process deposit successfully")
        void shouldProcessDepositSuccessfully() throws Exception {
            // Given
            TransactionRequest request = new TransactionRequest("testuser", "password", 100.0);
            when(bankingService.deposit("testuser", "password", 100.0)).thenReturn(true);
            when(bankingService.getAuthenticatedUser("testuser", "password")).thenReturn(mockUser);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("Deposit"))
                    .andExpect(jsonPath("$.amount").value(100.0));

            verify(bankingService).deposit("testuser", "password", 100.0);
        }

        @Test
        @DisplayName("Should return unauthorized for invalid credentials on deposit")
        void shouldReturnUnauthorizedForInvalidCredentialsOnDeposit() throws Exception {
            // Given
            TransactionRequest request = new TransactionRequest("testuser", "wrongpassword", 100.0);
            when(bankingService.deposit("testuser", "wrongpassword", 100.0)).thenReturn(false);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should process withdrawal successfully")
        void shouldProcessWithdrawalSuccessfully() throws Exception {
            // Given
            TransactionRequest request = new TransactionRequest("testuser", "password", 50.0);
            when(bankingService.withdraw("testuser", "password", 50.0)).thenReturn(true);
            when(bankingService.getAuthenticatedUser("testuser", "password")).thenReturn(mockUser);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/withdraw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("Withdrawal"))
                    .andExpect(jsonPath("$.amount").value(50.0));

            verify(bankingService).withdraw("testuser", "password", 50.0);
        }

        @Test
        @DisplayName("Should return bad request for insufficient funds")
        void shouldReturnBadRequestForInsufficientFunds() throws Exception {
            // Given
            TransactionRequest request = new TransactionRequest("testuser", "password", 1000.0);
            when(bankingService.withdraw("testuser", "password", 1000.0)).thenReturn(false);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/withdraw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Balance Tests")
    class BalanceTests {

        @Test
        @DisplayName("Should get balance successfully")
        void shouldGetBalanceSuccessfully() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("testuser", "password");
            when(bankingService.getBalance("testuser", "password")).thenReturn(150.0);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/balance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.balance").value(150.0));

            verify(bankingService).getBalance("testuser", "password");
        }

        @Test
        @DisplayName("Should return unauthorized for invalid credentials on balance check")
        void shouldReturnUnauthorizedForInvalidCredentialsOnBalanceCheck() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            when(bankingService.getBalance("testuser", "wrongpassword")).thenReturn(null);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/balance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
