package com.example.banking.api.service;

import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Banking Service Tests")
class BankingServiceTest {

    @Mock
    private BankingProcessService processService;

    @InjectMocks
    private BankingService bankingService;

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUserSuccessfully() {
            // Given
            when(processService.registerUser("newuser", "password123")).thenReturn(true);

            // When
            boolean result = bankingService.registerUser("newuser", "password123");

            // Then
            assertThat(result).isTrue();
            verify(processService).registerUser("newuser", "password123");
        }

        @Test
        @DisplayName("Should fail to register user with existing username")
        void shouldFailToRegisterUserWithExistingUsername() {
            // Given
            when(processService.registerUser("existinguser", "newpassword")).thenReturn(false);

            // When
            boolean result = bankingService.registerUser("existinguser", "newpassword");

            // Then
            assertThat(result).isFalse();
            verify(processService).registerUser("existinguser", "newpassword");
        }
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {

        @Test
        @DisplayName("Should authenticate user with correct credentials")
        void shouldAuthenticateUserWithCorrectCredentials() {
            // Given
            BankingUser mockUser = new BankingUser("testuser", 100.0);
            when(processService.authenticateUser("testuser", "password123")).thenReturn(mockUser);

            // When
            var user = bankingService.authenticateUser("testuser", "password123");

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getUsername()).isEqualTo("testuser");
            verify(processService).authenticateUser("testuser", "password123");
        }

        @Test
        @DisplayName("Should fail authentication with wrong password")
        void shouldFailAuthenticationWithWrongPassword() {
            // Given
            when(processService.authenticateUser("testuser", "wrongpassword")).thenReturn(null);

            // When
            var user = bankingService.authenticateUser("testuser", "wrongpassword");

            // Then
            assertThat(user).isNull();
            verify(processService).authenticateUser("testuser", "wrongpassword");
        }

        @Test
        @DisplayName("Should fail authentication with non-existent user")
        void shouldFailAuthenticationWithNonExistentUser() {
            // Given
            when(processService.authenticateUser("nonexistent", "password")).thenReturn(null);

            // When
            var user = bankingService.authenticateUser("nonexistent", "password");

            // Then
            assertThat(user).isNull();
            verify(processService).authenticateUser("nonexistent", "password");
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @BeforeEach
        void setUpUser() {
            bankingService.registerUser("testuser", "password123");
        }

        @Test
        @DisplayName("Should process deposit successfully")
        void shouldProcessDepositSuccessfully() {
            // When
            boolean result = bankingService.deposit("testuser", "password123", 100.0);

            // Then
            assertThat(result).isTrue();
            Double balance = bankingService.getBalance("testuser", "password123");
            assertThat(balance).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should fail deposit with invalid credentials")
        void shouldFailDepositWithInvalidCredentials() {
            // When
            boolean result = bankingService.deposit("testuser", "wrongpassword", 100.0);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fail deposit with negative amount")
        void shouldFailDepositWithNegativeAmount() {
            // When
            boolean result = bankingService.deposit("testuser", "password123", -50.0);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should process withdrawal successfully")
        void shouldProcessWithdrawalSuccessfully() {
            // Given
            bankingService.deposit("testuser", "password123", 100.0);

            // When
            boolean result = bankingService.withdraw("testuser", "password123", 50.0);

            // Then
            assertThat(result).isTrue();
            Double balance = bankingService.getBalance("testuser", "password123");
            assertThat(balance).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should fail withdrawal with insufficient funds")
        void shouldFailWithdrawalWithInsufficientFunds() {
            // Given
            bankingService.deposit("testuser", "password123", 50.0);

            // When
            boolean result = bankingService.withdraw("testuser", "password123", 100.0);

            // Then
            assertThat(result).isFalse();
            Double balance = bankingService.getBalance("testuser", "password123");
            assertThat(balance).isEqualTo(50.0); // Balance should remain unchanged
        }

        @Test
        @DisplayName("Should fail withdrawal with invalid credentials")
        void shouldFailWithdrawalWithInvalidCredentials() {
            // Given
            bankingService.deposit("testuser", "password123", 100.0);

            // When
            boolean result = bankingService.withdraw("testuser", "wrongpassword", 50.0);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Balance Tests")
    class BalanceTests {

        @Test
        @DisplayName("Should get balance for authenticated user")
        void shouldGetBalanceForAuthenticatedUser() {
            // Given
            bankingService.registerUser("testuser", "password123");
            bankingService.deposit("testuser", "password123", 150.0);

            // When
            Double balance = bankingService.getBalance("testuser", "password123");

            // Then
            assertThat(balance).isEqualTo(150.0);
        }

        @Test
        @DisplayName("Should return null for invalid credentials")
        void shouldReturnNullForInvalidCredentials() {
            // Given
            bankingService.registerUser("testuser", "password123");

            // When
            Double balance = bankingService.getBalance("testuser", "wrongpassword");

            // Then
            assertThat(balance).isNull();
        }
    }

    @Nested
    @DisplayName("Transaction History Tests")
    class TransactionHistoryTests {

        @Test
        @DisplayName("Should get transaction history for authenticated user")
        void shouldGetTransactionHistoryForAuthenticatedUser() {
            // Given
            bankingService.registerUser("testuser", "password123");
            bankingService.deposit("testuser", "password123", 100.0);
            bankingService.withdraw("testuser", "password123", 30.0);

            // When
            var transactions = bankingService.getTransactions("testuser", "password123");

            // Then
            assertThat(transactions).isNotNull();
            assertThat(transactions).hasSize(2);
            assertThat(transactions.get(0).getType()).isEqualTo("Deposit");
            assertThat(transactions.get(1).getType()).isEqualTo("Withdrawal");
        }

        @Test
        @DisplayName("Should return null for invalid credentials")
        void shouldReturnNullForInvalidCredentials() {
            // Given
            bankingService.registerUser("testuser", "password123");

            // When
            var transactions = bankingService.getTransactions("testuser", "wrongpassword");

            // Then
            assertThat(transactions).isNull();
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            // Given
            bankingService.registerUser("testuser", "password123");

            // When
            boolean result = bankingService.deleteUser("testuser", "password123");

            // Then
            assertThat(result).isTrue();
            var user = bankingService.authenticateUser("testuser", "password123");
            assertThat(user).isNull();
        }

        @Test
        @DisplayName("Should fail to delete user with invalid credentials")
        void shouldFailToDeleteUserWithInvalidCredentials() {
            // Given
            bankingService.registerUser("testuser", "password123");

            // When
            boolean result = bankingService.deleteUser("testuser", "wrongpassword");

            // Then
            assertThat(result).isFalse();
            var user = bankingService.authenticateUser("testuser", "password123");
            assertThat(user).isNotNull(); // User should still exist
        }
    }
}
