package com.example.banking.api.infrastructure.adapter.out.process;

import com.example.banking.api.domain.model.Account;
import com.example.banking.api.domain.model.Money;
import com.example.banking.api.domain.model.Transaction;
import com.example.banking.api.domain.model.User;
import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import com.example.banking.api.service.BankingProcessService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Banking Process Adapter Tests")
class BankingProcessAdapterTest {

    @Mock
    private BankingProcessService processService;

    @InjectMocks
    private BankingProcessAdapter adapter;

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            // Given
            User user = new User("testuser", "password123");
            when(processService.registerUser("testuser", "password123")).thenReturn(true);

            // When
            boolean result = adapter.registerUser(user);

            // Then
            assertThat(result).isTrue();
            verify(processService).registerUser("testuser", "password123");
        }

        @Test
        @DisplayName("Should handle registration failure")
        void shouldHandleRegistrationFailure() {
            // Given
            User user = new User("existinguser", "password123");
            when(processService.registerUser("existinguser", "password123")).thenReturn(false);

            // When
            boolean result = adapter.registerUser(user);

            // Then
            assertThat(result).isFalse();
            verify(processService).registerUser("existinguser", "password123");
        }
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {

        @Test
        @DisplayName("Should authenticate user successfully and return account")
        void shouldAuthenticateUserSuccessfullyAndReturnAccount() {
            // Given
            String username = "testuser";
            String password = "password123";
            BankingUser bankingUser = new BankingUser(username, 100.50);
            when(processService.authenticateUser(username, password)).thenReturn(bankingUser);

            // When
            Optional<Account> result = adapter.authenticateUser(username, password);

            // Then
            assertThat(result).isPresent();
            Account account = result.get();
            assertThat(account.getUsername()).isEqualTo(username);
            assertThat(account.getBalance()).isEqualTo(new Money(100.50));
            verify(processService).authenticateUser(username, password);
        }

        @Test
        @DisplayName("Should return empty optional for failed authentication")
        void shouldReturnEmptyOptionalForFailedAuthentication() {
            // Given
            String username = "testuser";
            String password = "wrongpassword";
            when(processService.authenticateUser(username, password)).thenReturn(null);

            // When
            Optional<Account> result = adapter.authenticateUser(username, password);

            // Then
            assertThat(result).isEmpty();
            verify(processService).authenticateUser(username, password);
        }
    }

    @Nested
    @DisplayName("Deposit Operation Tests")
    class DepositOperationTests {

        @Test
        @DisplayName("Should deposit money successfully")
        void shouldDepositMoneySuccessfully() {
            // Given
            String username = "testuser";
            String password = "password123";
            Money amount = new Money(50.00);
            when(processService.deposit(username, password, 50.00)).thenReturn(true);

            // When
            boolean result = adapter.deposit(username, password, amount);

            // Then
            assertThat(result).isTrue();
            verify(processService).deposit(username, password, 50.00);
        }

        @Test
        @DisplayName("Should handle deposit failure")
        void shouldHandleDepositFailure() {
            // Given
            String username = "testuser";
            String password = "wrongpassword";
            Money amount = new Money(50.00);
            when(processService.deposit(username, password, 50.00)).thenReturn(false);

            // When
            boolean result = adapter.deposit(username, password, amount);

            // Then
            assertThat(result).isFalse();
            verify(processService).deposit(username, password, 50.00);
        }

        @Test
        @DisplayName("Should handle fractional amounts correctly")
        void shouldHandleFractionalAmountsCorrectly() {
            // Given
            String username = "testuser";
            String password = "password123";
            Money amount = new Money(25.75);
            when(processService.deposit(username, password, 25.75)).thenReturn(true);

            // When
            boolean result = adapter.deposit(username, password, amount);

            // Then
            assertThat(result).isTrue();
            verify(processService).deposit(username, password, 25.75);
        }
    }

    @Nested
    @DisplayName("Withdrawal Operation Tests")
    class WithdrawalOperationTests {

        @Test
        @DisplayName("Should withdraw money successfully")
        void shouldWithdrawMoneySuccessfully() {
            // Given
            String username = "testuser";
            String password = "password123";
            Money amount = new Money(30.00);
            when(processService.withdraw(username, password, 30.00)).thenReturn(true);

            // When
            boolean result = adapter.withdraw(username, password, amount);

            // Then
            assertThat(result).isTrue();
            verify(processService).withdraw(username, password, 30.00);
        }

        @Test
        @DisplayName("Should handle withdrawal failure due to insufficient funds")
        void shouldHandleWithdrawalFailureDueToInsufficientFunds() {
            // Given
            String username = "testuser";
            String password = "password123";
            Money amount = new Money(1000.00);
            when(processService.withdraw(username, password, 1000.00)).thenReturn(false);

            // When
            boolean result = adapter.withdraw(username, password, amount);

            // Then
            assertThat(result).isFalse();
            verify(processService).withdraw(username, password, 1000.00);
        }

        @Test
        @DisplayName("Should handle withdrawal failure due to authentication")
        void shouldHandleWithdrawalFailureDueToAuthentication() {
            // Given
            String username = "testuser";
            String password = "wrongpassword";
            Money amount = new Money(50.00);
            when(processService.withdraw(username, password, 50.00)).thenReturn(false);

            // When
            boolean result = adapter.withdraw(username, password, amount);

            // Then
            assertThat(result).isFalse();
            verify(processService).withdraw(username, password, 50.00);
        }
    }

    @Nested
    @DisplayName("Balance Retrieval Tests")
    class BalanceRetrievalTests {

        @Test
        @DisplayName("Should get balance successfully")
        void shouldGetBalanceSuccessfully() {
            // Given
            String username = "testuser";
            String password = "password123";
            when(processService.getBalance(username, password)).thenReturn(150.75);

            // When
            Optional<Money> result = adapter.getBalance(username, password);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(new Money(150.75));
            verify(processService).getBalance(username, password);
        }

        @Test
        @DisplayName("Should return empty optional when balance retrieval fails")
        void shouldReturnEmptyOptionalWhenBalanceRetrievalFails() {
            // Given
            String username = "testuser";
            String password = "wrongpassword";
            when(processService.getBalance(username, password)).thenReturn(null);

            // When
            Optional<Money> result = adapter.getBalance(username, password);

            // Then
            assertThat(result).isEmpty();
            verify(processService).getBalance(username, password);
        }

        @Test
        @DisplayName("Should handle zero balance correctly")
        void shouldHandleZeroBalanceCorrectly() {
            // Given
            String username = "testuser";
            String password = "password123";
            when(processService.getBalance(username, password)).thenReturn(0.0);

            // When
            Optional<Money> result = adapter.getBalance(username, password);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(Money.zero());
            verify(processService).getBalance(username, password);
        }
    }

    @Nested
    @DisplayName("Transaction History Tests")
    class TransactionHistoryTests {

        @Test
        @DisplayName("Should get transaction history successfully")
        void shouldGetTransactionHistorySuccessfully() {
            // Given
            String username = "testuser";
            String password = "password123";
            LocalDateTime timestamp1 = LocalDateTime.of(2023, 12, 1, 10, 0, 0);
            LocalDateTime timestamp2 = LocalDateTime.of(2023, 12, 1, 11, 0, 0);
            
            List<BankingTransaction> bankingTransactions = Arrays.asList(
                new BankingTransaction("Deposit", 100.00, timestamp1),
                new BankingTransaction("Withdrawal", 25.00, timestamp2)
            );
            
            when(processService.getTransactions(username, password)).thenReturn(bankingTransactions);

            // When
            Optional<List<Transaction>> result = adapter.getTransactionHistory(username, password);

            // Then
            assertThat(result).isPresent();
            List<Transaction> transactions = result.get();
            assertThat(transactions).hasSize(2);
            
            Transaction transaction1 = transactions.get(0);
            assertThat(transaction1.getType()).isEqualTo(Transaction.Type.DEPOSIT);
            assertThat(transaction1.getAmount()).isEqualTo(new Money(100.00));
            assertThat(transaction1.getTimestamp()).isEqualTo(timestamp1);
            
            Transaction transaction2 = transactions.get(1);
            assertThat(transaction2.getType()).isEqualTo(Transaction.Type.WITHDRAWAL);
            assertThat(transaction2.getAmount()).isEqualTo(new Money(25.00));
            assertThat(transaction2.getTimestamp()).isEqualTo(timestamp2);
            
            verify(processService).getTransactions(username, password);
        }

        @Test
        @DisplayName("Should return empty optional when transaction history retrieval fails")
        void shouldReturnEmptyOptionalWhenTransactionHistoryRetrievalFails() {
            // Given
            String username = "testuser";
            String password = "wrongpassword";
            when(processService.getTransactions(username, password)).thenReturn(null);

            // When
            Optional<List<Transaction>> result = adapter.getTransactionHistory(username, password);

            // Then
            assertThat(result).isEmpty();
            verify(processService).getTransactions(username, password);
        }

        @Test
        @DisplayName("Should handle empty transaction history")
        void shouldHandleEmptyTransactionHistory() {
            // Given
            String username = "newuser";
            String password = "password123";
            when(processService.getTransactions(username, password)).thenReturn(Arrays.asList());

            // When
            Optional<List<Transaction>> result = adapter.getTransactionHistory(username, password);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEmpty();
            verify(processService).getTransactions(username, password);
        }

        @Test
        @DisplayName("Should handle unknown transaction types gracefully")
        void shouldHandleUnknownTransactionTypesGracefully() {
            // Given
            String username = "testuser";
            String password = "password123";
            LocalDateTime timestamp = LocalDateTime.of(2023, 12, 1, 10, 0, 0);
            
            List<BankingTransaction> bankingTransactions = Arrays.asList(
                new BankingTransaction("Unknown Operation", 50.00, timestamp)
            );
            
            when(processService.getTransactions(username, password)).thenReturn(bankingTransactions);

            // When
            Optional<List<Transaction>> result = adapter.getTransactionHistory(username, password);

            // Then
            assertThat(result).isPresent();
            List<Transaction> transactions = result.get();
            assertThat(transactions).hasSize(1);
            
            Transaction transaction = transactions.get(0);
            // Unknown types should default to DEPOSIT
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.DEPOSIT);
            assertThat(transaction.getAmount()).isEqualTo(new Money(50.00));
            assertThat(transaction.getDescription()).isEqualTo("Unknown Operation");
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {

        @Test
        @DisplayName("Should handle user deletion request")
        void shouldHandleUserDeletionRequest() {
            // Given
            String username = "testuser";
            String password = "password123";
            when(processService.deleteUser(username, password)).thenReturn(false);

            // When
            boolean result = adapter.deleteUser(username, password);

            // Then
            assertThat(result).isFalse(); // Currently not supported
            verify(processService).deleteUser(username, password);
        }
    }

    @Nested
    @DisplayName("Type Conversion Tests")
    class TypeConversionTests {

        @Test
        @DisplayName("Should correctly convert deposit transaction type")
        void shouldCorrectlyConvertDepositTransactionType() {
            // Given
            String username = "testuser";
            String password = "password123";
            LocalDateTime timestamp = LocalDateTime.of(2023, 12, 1, 10, 0, 0);
            
            List<BankingTransaction> bankingTransactions = Arrays.asList(
                new BankingTransaction("Deposit transaction", 100.00, timestamp),
                new BankingTransaction("DEPOSIT", 50.00, timestamp),
                new BankingTransaction("Account deposit", 25.00, timestamp)
            );
            
            when(processService.getTransactions(username, password)).thenReturn(bankingTransactions);

            // When
            Optional<List<Transaction>> result = adapter.getTransactionHistory(username, password);

            // Then
            assertThat(result).isPresent();
            List<Transaction> transactions = result.get();
            
            transactions.forEach(transaction -> 
                assertThat(transaction.getType()).isEqualTo(Transaction.Type.DEPOSIT)
            );
        }

        @Test
        @DisplayName("Should correctly convert withdrawal transaction type")
        void shouldCorrectlyConvertWithdrawalTransactionType() {
            // Given
            String username = "testuser";
            String password = "password123";
            LocalDateTime timestamp = LocalDateTime.of(2023, 12, 1, 10, 0, 0);
            
            List<BankingTransaction> bankingTransactions = Arrays.asList(
                new BankingTransaction("Withdrawal transaction", 50.00, timestamp),
                new BankingTransaction("WITHDRAW", 25.00, timestamp),
                new BankingTransaction("Account withdrawal", 10.00, timestamp)
            );
            
            when(processService.getTransactions(username, password)).thenReturn(bankingTransactions);

            // When
            Optional<List<Transaction>> result = adapter.getTransactionHistory(username, password);

            // Then
            assertThat(result).isPresent();
            List<Transaction> transactions = result.get();
            
            transactions.forEach(transaction -> 
                assertThat(transaction.getType()).isEqualTo(Transaction.Type.WITHDRAWAL)
            );
        }

        @Test
        @DisplayName("Should handle case-insensitive transaction type detection")
        void shouldHandleCaseInsensitiveTransactionTypeDetection() {
            // Given
            String username = "testuser";
            String password = "password123";
            LocalDateTime timestamp = LocalDateTime.of(2023, 12, 1, 10, 0, 0);
            
            List<BankingTransaction> bankingTransactions = Arrays.asList(
                new BankingTransaction("DEPOSIT", 100.00, timestamp),
                new BankingTransaction("deposit", 50.00, timestamp),
                new BankingTransaction("Deposit", 25.00, timestamp),
                new BankingTransaction("WITHDRAWAL", 30.00, timestamp),
                new BankingTransaction("withdrawal", 20.00, timestamp),
                new BankingTransaction("Withdraw", 10.00, timestamp)
            );
            
            when(processService.getTransactions(username, password)).thenReturn(bankingTransactions);

            // When
            Optional<List<Transaction>> result = adapter.getTransactionHistory(username, password);

            // Then
            assertThat(result).isPresent();
            List<Transaction> transactions = result.get();
            
            // First 3 should be deposits
            for (int i = 0; i < 3; i++) {
                assertThat(transactions.get(i).getType()).isEqualTo(Transaction.Type.DEPOSIT);
            }
            
            // Last 3 should be withdrawals
            for (int i = 3; i < 6; i++) {
                assertThat(transactions.get(i).getType()).isEqualTo(Transaction.Type.WITHDRAWAL);
            }
        }
    }

    @Nested
    @DisplayName("Money Conversion Tests")
    class MoneyConversionTests {

        @Test
        @DisplayName("Should convert double amounts to Money correctly")
        void shouldConvertDoubleAmountsToMoneyCorrectly() {
            // Given
            String username = "testuser";
            String password = "password123";
            when(processService.getBalance(username, password)).thenReturn(123.45);

            // When
            Optional<Money> result = adapter.getBalance(username, password);

            // Then
            assertThat(result).isPresent();
            Money money = result.get();
            assertThat(money.toDouble()).isEqualTo(123.45);
            assertThat(money.toString()).isEqualTo("$123.45");
        }

        @Test
        @DisplayName("Should handle precision in double to Money conversion")
        void shouldHandlePrecisionInDoubleToMoneyConversion() {
            // Given
            String username = "testuser";
            String password = "password123";
            // This tests floating point precision issues
            when(processService.getBalance(username, password)).thenReturn(0.1 + 0.2); // 0.30000000000000004

            // When
            Optional<Money> result = adapter.getBalance(username, password);

            // Then
            assertThat(result).isPresent();
            Money money = result.get();
            // Money should handle precision correctly
            assertThat(money.toString()).isEqualTo("$0.30");
        }
    }
}