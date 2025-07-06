package com.example.banking.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Transaction Domain Model Tests")
class TransactionTest {

    private final LocalDateTime testTimestamp = LocalDateTime.of(2023, 12, 1, 10, 30, 0);
    private final Money testAmount = new Money(100.50);

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Should create transaction with all parameters")
        void shouldCreateTransactionWithAllParameters() {
            Transaction transaction = new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    testTimestamp,
                    "Test deposit"
            );

            assertThat(transaction.getType()).isEqualTo(Transaction.Type.DEPOSIT);
            assertThat(transaction.getAmount()).isEqualTo(testAmount);
            assertThat(transaction.getTimestamp()).isEqualTo(testTimestamp);
            assertThat(transaction.getDescription()).isEqualTo("Test deposit");
        }

        @Test
        @DisplayName("Should create transaction without description")
        void shouldCreateTransactionWithoutDescription() {
            Transaction transaction = new Transaction(
                    Transaction.Type.WITHDRAWAL,
                    testAmount,
                    testTimestamp
            );

            assertThat(transaction.getType()).isEqualTo(Transaction.Type.WITHDRAWAL);
            assertThat(transaction.getAmount()).isEqualTo(testAmount);
            assertThat(transaction.getTimestamp()).isEqualTo(testTimestamp);
            assertThat(transaction.getDescription()).isEqualTo("");
        }

        @Test
        @DisplayName("Should handle null description gracefully")
        void shouldHandleNullDescriptionGracefully() {
            Transaction transaction = new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    testTimestamp,
                    null
            );

            assertThat(transaction.getDescription()).isEqualTo("");
        }

        @Test
        @DisplayName("Should throw exception for null type")
        void shouldThrowExceptionForNullType() {
            assertThatThrownBy(() -> new Transaction(
                    null,
                    testAmount,
                    testTimestamp,
                    "description"
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Transaction type cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for null amount")
        void shouldThrowExceptionForNullAmount() {
            assertThatThrownBy(() -> new Transaction(
                    Transaction.Type.DEPOSIT,
                    null,
                    testTimestamp,
                    "description"
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Transaction amount cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for null timestamp")
        void shouldThrowExceptionForNullTimestamp() {
            assertThatThrownBy(() -> new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    null,
                    "description"
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessage("Transaction timestamp cannot be null");
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create deposit transaction using factory method")
        void shouldCreateDepositTransactionUsingFactoryMethod() {
            Transaction transaction = Transaction.deposit(testAmount, testTimestamp);

            assertThat(transaction.getType()).isEqualTo(Transaction.Type.DEPOSIT);
            assertThat(transaction.getAmount()).isEqualTo(testAmount);
            assertThat(transaction.getTimestamp()).isEqualTo(testTimestamp);
            assertThat(transaction.getDescription()).isEqualTo("Deposit");
        }

        @Test
        @DisplayName("Should create withdrawal transaction using factory method")
        void shouldCreateWithdrawalTransactionUsingFactoryMethod() {
            Transaction transaction = Transaction.withdrawal(testAmount, testTimestamp);

            assertThat(transaction.getType()).isEqualTo(Transaction.Type.WITHDRAWAL);
            assertThat(transaction.getAmount()).isEqualTo(testAmount);
            assertThat(transaction.getTimestamp()).isEqualTo(testTimestamp);
            assertThat(transaction.getDescription()).isEqualTo("Withdrawal");
        }
    }

    @Nested
    @DisplayName("Type Check Tests")
    class TypeCheckTests {

        @Test
        @DisplayName("Should correctly identify deposit transactions")
        void shouldCorrectlyIdentifyDepositTransactions() {
            Transaction depositTransaction = Transaction.deposit(testAmount, testTimestamp);
            Transaction withdrawalTransaction = Transaction.withdrawal(testAmount, testTimestamp);

            assertThat(depositTransaction.isDeposit()).isTrue();
            assertThat(depositTransaction.isWithdrawal()).isFalse();
            
            assertThat(withdrawalTransaction.isDeposit()).isFalse();
            assertThat(withdrawalTransaction.isWithdrawal()).isTrue();
        }

        @Test
        @DisplayName("Should correctly identify withdrawal transactions")
        void shouldCorrectlyIdentifyWithdrawalTransactions() {
            Transaction transaction = new Transaction(
                    Transaction.Type.WITHDRAWAL,
                    testAmount,
                    testTimestamp
            );

            assertThat(transaction.isWithdrawal()).isTrue();
            assertThat(transaction.isDeposit()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when all properties are the same")
        void shouldBeEqualWhenAllPropertiesAreTheSame() {
            Transaction transaction1 = new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    testTimestamp,
                    "Test transaction"
            );
            Transaction transaction2 = new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    testTimestamp,
                    "Test transaction"
            );

            assertThat(transaction1).isEqualTo(transaction2);
            assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when types are different")
        void shouldNotBeEqualWhenTypesAreDifferent() {
            Transaction deposit = Transaction.deposit(testAmount, testTimestamp);
            Transaction withdrawal = Transaction.withdrawal(testAmount, testTimestamp);

            assertThat(deposit).isNotEqualTo(withdrawal);
        }

        @Test
        @DisplayName("Should not be equal when amounts are different")
        void shouldNotBeEqualWhenAmountsAreDifferent() {
            Money amount1 = new Money(100.00);
            Money amount2 = new Money(200.00);
            
            Transaction transaction1 = Transaction.deposit(amount1, testTimestamp);
            Transaction transaction2 = Transaction.deposit(amount2, testTimestamp);

            assertThat(transaction1).isNotEqualTo(transaction2);
        }

        @Test
        @DisplayName("Should not be equal when timestamps are different")
        void shouldNotBeEqualWhenTimestampsAreDifferent() {
            LocalDateTime timestamp1 = LocalDateTime.of(2023, 12, 1, 10, 0, 0);
            LocalDateTime timestamp2 = LocalDateTime.of(2023, 12, 1, 11, 0, 0);
            
            Transaction transaction1 = Transaction.deposit(testAmount, timestamp1);
            Transaction transaction2 = Transaction.deposit(testAmount, timestamp2);

            assertThat(transaction1).isNotEqualTo(transaction2);
        }

        @Test
        @DisplayName("Should not be equal when descriptions are different")
        void shouldNotBeEqualWhenDescriptionsAreDifferent() {
            Transaction transaction1 = new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    testTimestamp,
                    "Description 1"
            );
            Transaction transaction2 = new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    testTimestamp,
                    "Description 2"
            );

            assertThat(transaction1).isNotEqualTo(transaction2);
        }

        @Test
        @DisplayName("Should handle null and different types in equals")
        void shouldHandleNullAndDifferentTypesInEquals() {
            Transaction transaction = Transaction.deposit(testAmount, testTimestamp);

            assertThat(transaction).isNotEqualTo(null);
            assertThat(transaction).isNotEqualTo("not a transaction");
            assertThat(transaction).isNotEqualTo(123);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            Transaction transaction = Transaction.deposit(testAmount, testTimestamp);

            assertThat(transaction).isEqualTo(transaction);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - getters return same values")
        void shouldBeImmutableGettersReturnSameValues() {
            Transaction transaction = new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    testTimestamp,
                    "Test description"
            );

            // Call getters multiple times to ensure they return the same values
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.DEPOSIT);
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.DEPOSIT);
            
            assertThat(transaction.getAmount()).isEqualTo(testAmount);
            assertThat(transaction.getAmount()).isEqualTo(testAmount);
            
            assertThat(transaction.getTimestamp()).isEqualTo(testTimestamp);
            assertThat(transaction.getTimestamp()).isEqualTo(testTimestamp);
            
            assertThat(transaction.getDescription()).isEqualTo("Test description");
            assertThat(transaction.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("Should maintain immutability of Money amount")
        void shouldMaintainImmutabilityOfMoneyAmount() {
            Money originalAmount = new Money(100.00);
            Transaction transaction = Transaction.deposit(originalAmount, testTimestamp);

            // Getting the amount should return the same Money object or an equivalent one
            Money retrievedAmount = transaction.getAmount();
            assertThat(retrievedAmount).isEqualTo(originalAmount);
            
            // Money itself should be immutable, so this doesn't affect the transaction
            Money newAmount = retrievedAmount.add(new Money(50.00));
            assertThat(transaction.getAmount()).isEqualTo(originalAmount);
            assertThat(transaction.getAmount()).isNotEqualTo(newAmount);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            Transaction transaction = new Transaction(
                    Transaction.Type.DEPOSIT,
                    testAmount,
                    testTimestamp,
                    "Test deposit"
            );

            String result = transaction.toString();

            assertThat(result).contains("DEPOSIT");
            assertThat(result).contains("$100.50");
            assertThat(result).contains("2023-12-01T10:30");
            assertThat(result).contains("Test deposit");
        }

        @Test
        @DisplayName("Should handle empty description in toString")
        void shouldHandleEmptyDescriptionInToString() {
            Transaction transaction = new Transaction(
                    Transaction.Type.WITHDRAWAL,
                    testAmount,
                    testTimestamp
            );

            String result = transaction.toString();

            assertThat(result).contains("WITHDRAWAL");
            assertThat(result).contains("description=''");
        }
    }
}