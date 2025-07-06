package com.example.banking.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account Domain Model Tests")
class AccountTest {

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Should create account with username only")
        void shouldCreateAccountWithUsernameOnly() {
            Account account = new Account("testuser");

            assertThat(account.getUsername()).isEqualTo("testuser");
            assertThat(account.getBalance()).isEqualTo(Money.zero());
            assertThat(account.getTransactions()).isEmpty();
        }

        @Test
        @DisplayName("Should create account with username and initial balance")
        void shouldCreateAccountWithUsernameAndInitialBalance() {
            Money initialBalance = new Money(100.50);
            Account account = new Account("testuser", initialBalance);

            assertThat(account.getUsername()).isEqualTo("testuser");
            assertThat(account.getBalance()).isEqualTo(initialBalance);
            assertThat(account.getTransactions()).isEmpty();
        }

        @Test
        @DisplayName("Should trim whitespace from username")
        void shouldTrimWhitespaceFromUsername() {
            Account account = new Account("  testuser  ");

            assertThat(account.getUsername()).isEqualTo("testuser");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception for invalid usernames")
        void shouldThrowExceptionForInvalidUsernames(String invalidUsername) {
            assertThatThrownBy(() -> new Account(invalidUsername))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw exception for null initial balance")
        void shouldThrowExceptionForNullInitialBalance() {
            assertThatThrownBy(() -> new Account("testuser", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Initial balance cannot be null");
        }
    }

    @Nested
    @DisplayName("Deposit Operation Tests")
    class DepositOperationTests {

        @Test
        @DisplayName("Should successfully deposit money")
        void shouldSuccessfullyDepositMoney() {
            Account account = new Account("testuser");
            Money depositAmount = new Money(50.00);

            Transaction transaction = account.deposit(depositAmount);

            assertThat(account.getBalance()).isEqualTo(depositAmount);
            assertThat(transaction).isNotNull();
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.DEPOSIT);
            assertThat(transaction.getAmount()).isEqualTo(depositAmount);
            assertThat(account.getTransactions()).hasSize(1);
            assertThat(account.getTransactions().get(0)).isEqualTo(transaction);
        }

        @Test
        @DisplayName("Should handle multiple deposits")
        void shouldHandleMultipleDeposits() {
            Account account = new Account("testuser");
            Money deposit1 = new Money(30.00);
            Money deposit2 = new Money(20.00);

            Transaction transaction1 = account.deposit(deposit1);
            Transaction transaction2 = account.deposit(deposit2);

            assertThat(account.getBalance()).isEqualTo(new Money(50.00));
            assertThat(account.getTransactions()).hasSize(2);
            assertThat(account.getTransactions().get(0)).isEqualTo(transaction1);
            assertThat(account.getTransactions().get(1)).isEqualTo(transaction2);
        }

        @Test
        @DisplayName("Should deposit on top of initial balance")
        void shouldDepositOnTopOfInitialBalance() {
            Money initialBalance = new Money(100.00);
            Account account = new Account("testuser", initialBalance);
            Money depositAmount = new Money(25.00);

            account.deposit(depositAmount);

            assertThat(account.getBalance()).isEqualTo(new Money(125.00));
        }

        @Test
        @DisplayName("Should throw exception for null deposit amount")
        void shouldThrowExceptionForNullDepositAmount() {
            Account account = new Account("testuser");

            assertThatThrownBy(() -> account.deposit(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Deposit amount must be positive");
        }

        @Test
        @DisplayName("Should throw exception for zero deposit amount")
        void shouldThrowExceptionForZeroDepositAmount() {
            Account account = new Account("testuser");

            assertThatThrownBy(() -> account.deposit(Money.zero()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Deposit amount must be positive");
        }

        @Test
        @DisplayName("Should record transaction timestamp during deposit")
        void shouldRecordTransactionTimestampDuringDeposit() {
            Account account = new Account("testuser");
            LocalDateTime beforeDeposit = LocalDateTime.now();

            Transaction transaction = account.deposit(new Money(100.00));

            LocalDateTime afterDeposit = LocalDateTime.now();
            assertThat(transaction.getTimestamp()).isBetween(beforeDeposit, afterDeposit);
        }
    }

    @Nested
    @DisplayName("Withdrawal Operation Tests")
    class WithdrawalOperationTests {

        @Test
        @DisplayName("Should successfully withdraw money")
        void shouldSuccessfullyWithdrawMoney() {
            Money initialBalance = new Money(100.00);
            Account account = new Account("testuser", initialBalance);
            Money withdrawalAmount = new Money(30.00);

            Transaction transaction = account.withdraw(withdrawalAmount);

            assertThat(account.getBalance()).isEqualTo(new Money(70.00));
            assertThat(transaction).isNotNull();
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.WITHDRAWAL);
            assertThat(transaction.getAmount()).isEqualTo(withdrawalAmount);
            assertThat(account.getTransactions()).hasSize(1);
            assertThat(account.getTransactions().get(0)).isEqualTo(transaction);
        }

        @Test
        @DisplayName("Should handle multiple withdrawals")
        void shouldHandleMultipleWithdrawals() {
            Money initialBalance = new Money(100.00);
            Account account = new Account("testuser", initialBalance);
            Money withdrawal1 = new Money(20.00);
            Money withdrawal2 = new Money(30.00);

            Transaction transaction1 = account.withdraw(withdrawal1);
            Transaction transaction2 = account.withdraw(withdrawal2);

            assertThat(account.getBalance()).isEqualTo(new Money(50.00));
            assertThat(account.getTransactions()).hasSize(2);
            assertThat(account.getTransactions().get(0)).isEqualTo(transaction1);
            assertThat(account.getTransactions().get(1)).isEqualTo(transaction2);
        }

        @Test
        @DisplayName("Should allow withdrawal of entire balance")
        void shouldAllowWithdrawalOfEntireBalance() {
            Money initialBalance = new Money(100.00);
            Account account = new Account("testuser", initialBalance);

            account.withdraw(initialBalance);

            assertThat(account.getBalance()).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("Should throw exception for insufficient funds")
        void shouldThrowExceptionForInsufficientFunds() {
            Money initialBalance = new Money(50.00);
            Account account = new Account("testuser", initialBalance);
            Money withdrawalAmount = new Money(100.00);

            assertThatThrownBy(() -> account.withdraw(withdrawalAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient funds. Current balance: $50.00, Requested: $100.00");
        }

        @Test
        @DisplayName("Should throw exception for null withdrawal amount")
        void shouldThrowExceptionForNullWithdrawalAmount() {
            Account account = new Account("testuser", new Money(100.00));

            assertThatThrownBy(() -> account.withdraw(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Withdrawal amount must be positive");
        }

        @Test
        @DisplayName("Should throw exception for zero withdrawal amount")
        void shouldThrowExceptionForZeroWithdrawalAmount() {
            Account account = new Account("testuser", new Money(100.00));

            assertThatThrownBy(() -> account.withdraw(Money.zero()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Withdrawal amount must be positive");
        }

        @Test
        @DisplayName("Should throw exception when withdrawing from zero balance")
        void shouldThrowExceptionWhenWithdrawingFromZeroBalance() {
            Account account = new Account("testuser");
            Money withdrawalAmount = new Money(10.00);

            assertThatThrownBy(() -> account.withdraw(withdrawalAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient funds. Current balance: $0.00, Requested: $10.00");
        }
    }

    @Nested
    @DisplayName("Transaction History Tests")
    class TransactionHistoryTests {

        @Test
        @DisplayName("Should maintain transaction order")
        void shouldMaintainTransactionOrder() {
            Account account = new Account("testuser", new Money(100.00));

            Transaction deposit = account.deposit(new Money(50.00));
            Transaction withdrawal = account.withdraw(new Money(25.00));

            List<Transaction> transactions = account.getTransactions();
            assertThat(transactions).hasSize(2);
            assertThat(transactions.get(0)).isEqualTo(deposit);
            assertThat(transactions.get(1)).isEqualTo(withdrawal);
        }

        @Test
        @DisplayName("Should return immutable transaction list")
        void shouldReturnImmutableTransactionList() {
            Account account = new Account("testuser");
            account.deposit(new Money(50.00));

            List<Transaction> transactions = account.getTransactions();
            
            assertThatThrownBy(() -> transactions.add(Transaction.deposit(new Money(10.00), LocalDateTime.now())))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should get recent transactions with limit")
        void shouldGetRecentTransactionsWithLimit() {
            Account account = new Account("testuser", new Money(1000.00));

            // Create 5 transactions
            for (int i = 1; i <= 5; i++) {
                account.deposit(new Money(i * 10.0));
            }

            List<Transaction> recentTransactions = account.getRecentTransactions(3);

            assertThat(recentTransactions).hasSize(3);
            assertThat(recentTransactions.get(0).getAmount()).isEqualTo(new Money(30.00));
            assertThat(recentTransactions.get(1).getAmount()).isEqualTo(new Money(40.00));
            assertThat(recentTransactions.get(2).getAmount()).isEqualTo(new Money(50.00));
        }

        @Test
        @DisplayName("Should handle recent transactions when fewer than limit")
        void shouldHandleRecentTransactionsWhenFewerThanLimit() {
            Account account = new Account("testuser");
            account.deposit(new Money(50.00));

            List<Transaction> recentTransactions = account.getRecentTransactions(5);

            assertThat(recentTransactions).hasSize(1);
            assertThat(recentTransactions.get(0).getAmount()).isEqualTo(new Money(50.00));
        }

        @Test
        @DisplayName("Should return empty list for zero or negative limit")
        void shouldReturnEmptyListForZeroOrNegativeLimit() {
            Account account = new Account("testuser");
            account.deposit(new Money(50.00));

            assertThat(account.getRecentTransactions(0)).isEmpty();
            assertThat(account.getRecentTransactions(-1)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when no transactions exist")
        void shouldReturnEmptyListWhenNoTransactionsExist() {
            Account account = new Account("testuser");

            assertThat(account.getRecentTransactions(5)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when usernames are the same")
        void shouldBeEqualWhenUsernamesAreTheSame() {
            Account account1 = new Account("testuser", new Money(100.00));
            Account account2 = new Account("testuser", new Money(200.00));

            assertThat(account1).isEqualTo(account2);
            assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when usernames are different")
        void shouldNotBeEqualWhenUsernamesAreDifferent() {
            Account account1 = new Account("testuser1");
            Account account2 = new Account("testuser2");

            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("Should handle null and different types in equals")
        void shouldHandleNullAndDifferentTypesInEquals() {
            Account account = new Account("testuser");

            assertThat(account).isNotEqualTo(null);
            assertThat(account).isNotEqualTo("testuser");
            assertThat(account).isNotEqualTo(123);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            Account account = new Account("testuser", new Money(100.50));
            account.deposit(new Money(25.00));

            String result = account.toString();

            assertThat(result).contains("testuser");
            assertThat(result).contains("$125.50");
            assertThat(result).contains("transactionCount=1");
        }
    }

    @Nested
    @DisplayName("Complex Scenario Tests")
    class ComplexScenarioTests {

        @Test
        @DisplayName("Should handle complex transaction sequence")
        void shouldHandleComplexTransactionSequence() {
            Account account = new Account("testuser", new Money(1000.00));

            // Multiple deposits and withdrawals
            account.deposit(new Money(200.00));    // Balance: 1200.00
            account.withdraw(new Money(150.00));   // Balance: 1050.00
            account.deposit(new Money(75.00));     // Balance: 1125.00
            account.withdraw(new Money(300.00));   // Balance: 825.00

            assertThat(account.getBalance()).isEqualTo(new Money(825.00));
            assertThat(account.getTransactions()).hasSize(4);

            // Verify transaction types
            List<Transaction> transactions = account.getTransactions();
            assertThat(transactions.get(0).isDeposit()).isTrue();
            assertThat(transactions.get(1).isWithdrawal()).isTrue();
            assertThat(transactions.get(2).isDeposit()).isTrue();
            assertThat(transactions.get(3).isWithdrawal()).isTrue();
        }

        @Test
        @DisplayName("Should maintain balance consistency through operations")
        void shouldMaintainBalanceConsistencyThroughOperations() {
            Account account = new Account("testuser");

            Money runningBalance = Money.zero();
            
            // Perform random operations and verify balance consistency
            Money[] deposits = {new Money(100.00), new Money(50.00), new Money(25.00)};
            Money[] withdrawals = {new Money(30.00), new Money(20.00)};

            for (Money deposit : deposits) {
                account.deposit(deposit);
                runningBalance = runningBalance.add(deposit);
                assertThat(account.getBalance()).isEqualTo(runningBalance);
            }

            for (Money withdrawal : withdrawals) {
                account.withdraw(withdrawal);
                runningBalance = runningBalance.subtract(withdrawal);
                assertThat(account.getBalance()).isEqualTo(runningBalance);
            }

            assertThat(account.getBalance()).isEqualTo(new Money(125.00));
        }
    }
}