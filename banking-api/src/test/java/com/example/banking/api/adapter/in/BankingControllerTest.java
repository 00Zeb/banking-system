package com.example.banking.api.adapter.in;

import com.example.banking.api.dto.LoginRequest;
import com.example.banking.api.dto.RegisterRequest;
import com.example.banking.api.dto.TransactionRequest;
import com.example.banking.api.application.port.in.UserAccountUseCase;
import com.example.banking.api.domain.BankingUser;
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

@WebMvcTest(com.example.banking.api.adapter.in.BankingController.class)
@DisplayName("Banking Controller Tests")
class BankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAccountUseCase userAccountUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private BankingUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new BankingUser("testuser", 100.0);
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest("newuser", "password123");
            when(userAccountUseCase.registerUser("newuser", "password123")).thenReturn(true);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User registered successfully"));

            verify(userAccountUseCase).registerUser("newuser", "password123");
        }

        @Test
        @DisplayName("Should return conflict when username exists")
        void shouldReturnConflictWhenUsernameExists() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest("existinguser", "password123");
            when(userAccountUseCase.registerUser("existinguser", "password123")).thenReturn(false);

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
            when(userAccountUseCase.authenticateUser("testuser", "password")).thenReturn(mockUser);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"));

            verify(userAccountUseCase).authenticateUser("testuser", "password");
        }

        @Test
        @DisplayName("Should return unauthorized for invalid credentials")
        void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            when(userAccountUseCase.authenticateUser("testuser", "wrongpassword")).thenReturn(null);

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
            when(userAccountUseCase.deposit("testuser", "password", 100.0)).thenReturn(true);
            when(userAccountUseCase.getBalance("testuser", "password")).thenReturn(100.0);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("Deposit"))
                    .andExpect(jsonPath("$.amount").value(100.0));

            verify(userAccountUseCase).deposit("testuser", "password", 100.0);
        }

        @Test
        @DisplayName("Should return unauthorized for invalid credentials on deposit")
        void shouldReturnUnauthorizedForInvalidCredentialsOnDeposit() throws Exception {
            // Given
            TransactionRequest request = new TransactionRequest("testuser", "wrongpassword", 100.0);
            when(userAccountUseCase.deposit("testuser", "wrongpassword", 100.0)).thenReturn(false);

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
            when(userAccountUseCase.withdraw("testuser", "password", 50.0)).thenReturn(true);
            when(userAccountUseCase.getBalance("testuser", "password")).thenReturn(50.0);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/withdraw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("Withdrawal"))
                    .andExpect(jsonPath("$.amount").value(50.0));

            verify(userAccountUseCase).withdraw("testuser", "password", 50.0);
        }

        @Test
        @DisplayName("Should return bad request for insufficient funds")
        void shouldReturnBadRequestForInsufficientFunds() throws Exception {
            // Given
            TransactionRequest request = new TransactionRequest("testuser", "password", 1000.0);
            when(userAccountUseCase.withdraw("testuser", "password", 1000.0)).thenReturn(false);

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
            when(userAccountUseCase.getBalance("testuser", "password")).thenReturn(150.0);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/balance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.balance").value(150.0));

            verify(userAccountUseCase).getBalance("testuser", "password");
        }

        @Test
        @DisplayName("Should return unauthorized for invalid credentials on balance check")
        void shouldReturnUnauthorizedForInvalidCredentialsOnBalanceCheck() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            when(userAccountUseCase.getBalance("testuser", "wrongpassword")).thenReturn(null);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/balance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
