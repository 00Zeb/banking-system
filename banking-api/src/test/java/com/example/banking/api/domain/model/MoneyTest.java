package com.example.banking.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Should create money from BigDecimal")
        void shouldCreateMoneyFromBigDecimal() {
            Money money = new Money(new BigDecimal("10.50"));
            
            assertThat(money.getAmount()).isEqualTo(new BigDecimal("10.50"));
            assertThat(money.toDouble()).isEqualTo(10.50);
            assertThat(money.toString()).isEqualTo("$10.50");
        }

        @Test
        @DisplayName("Should create money from double")
        void shouldCreateMoneyFromDouble() {
            Money money = new Money(25.75);
            
            assertThat(money.toDouble()).isEqualTo(25.75);
            assertThat(money.toString()).isEqualTo("$25.75");
        }

        @Test
        @DisplayName("Should create money from string")
        void shouldCreateMoneyFromString() {
            Money money = new Money("100.00");
            
            assertThat(money.toDouble()).isEqualTo(100.00);
            assertThat(money.toString()).isEqualTo("$100.00");
        }

        @Test
        @DisplayName("Should create zero money")
        void shouldCreateZeroMoney() {
            Money zero = Money.zero();
            
            assertThat(zero.isZero()).isTrue();
            assertThat(zero.toDouble()).isEqualTo(0.0);
            assertThat(zero.toString()).isEqualTo("$0.00");
        }

        @Test
        @DisplayName("Should round to 2 decimal places")
        void shouldRoundToTwoDecimalPlaces() {
            Money money1 = new Money(10.555);
            Money money2 = new Money(10.554);
            
            assertThat(money1.toString()).isEqualTo("$10.56");
            assertThat(money2.toString()).isEqualTo("$10.55");
        }

        @Test
        @DisplayName("Should throw exception for null amount")
        void shouldThrowExceptionForNullAmount() {
            assertThatThrownBy(() -> new Money((BigDecimal) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount cannot be null");
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.0, -10.50, -0.01})
        @DisplayName("Should throw exception for negative amounts")
        void shouldThrowExceptionForNegativeAmounts(double negativeAmount) {
            assertThatThrownBy(() -> new Money(negativeAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount cannot be negative");
        }
    }

    @Nested
    @DisplayName("Arithmetic Operations Tests")
    class ArithmeticOperationsTests {

        @Test
        @DisplayName("Should add money amounts correctly")
        void shouldAddMoneyAmountsCorrectly() {
            Money money1 = new Money(10.50);
            Money money2 = new Money(5.25);
            
            Money result = money1.add(money2);
            
            assertThat(result.toDouble()).isEqualTo(15.75);
            assertThat(result.toString()).isEqualTo("$15.75");
        }

        @Test
        @DisplayName("Should subtract money amounts correctly")
        void shouldSubtractMoneyAmountsCorrectly() {
            Money money1 = new Money(20.00);
            Money money2 = new Money(7.50);
            
            Money result = money1.subtract(money2);
            
            assertThat(result.toDouble()).isEqualTo(12.50);
            assertThat(result.toString()).isEqualTo("$12.50");
        }

        @Test
        @DisplayName("Should throw exception when subtracting more than available")
        void shouldThrowExceptionWhenSubtractingMoreThanAvailable() {
            Money money1 = new Money(10.00);
            Money money2 = new Money(15.00);
            
            assertThatThrownBy(() -> money1.subtract(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient funds");
        }

        @Test
        @DisplayName("Should handle subtraction resulting in zero")
        void shouldHandleSubtractionResultingInZero() {
            Money money1 = new Money(10.00);
            Money money2 = new Money(10.00);
            
            Money result = money1.subtract(money2);
            
            assertThat(result.isZero()).isTrue();
            assertThat(result.toString()).isEqualTo("$0.00");
        }
    }

    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {

        @Test
        @DisplayName("Should correctly identify greater than")
        void shouldCorrectlyIdentifyGreaterThan() {
            Money money1 = new Money(15.00);
            Money money2 = new Money(10.00);
            
            assertThat(money1.isGreaterThan(money2)).isTrue();
            assertThat(money2.isGreaterThan(money1)).isFalse();
        }

        @Test
        @DisplayName("Should correctly identify greater than or equal")
        void shouldCorrectlyIdentifyGreaterThanOrEqual() {
            Money money1 = new Money(15.00);
            Money money2 = new Money(15.00);
            Money money3 = new Money(10.00);
            
            assertThat(money1.isGreaterThanOrEqual(money2)).isTrue();
            assertThat(money1.isGreaterThanOrEqual(money3)).isTrue();
            assertThat(money3.isGreaterThanOrEqual(money1)).isFalse();
        }

        @Test
        @DisplayName("Should correctly identify less than")
        void shouldCorrectlyIdentifyLessThan() {
            Money money1 = new Money(10.00);
            Money money2 = new Money(15.00);
            
            assertThat(money1.isLessThan(money2)).isTrue();
            assertThat(money2.isLessThan(money1)).isFalse();
        }

        @Test
        @DisplayName("Should correctly identify zero amounts")
        void shouldCorrectlyIdentifyZeroAmounts() {
            Money zero1 = Money.zero();
            Money zero2 = new Money(0.0);
            Money nonZero = new Money(1.0);
            
            assertThat(zero1.isZero()).isTrue();
            assertThat(zero2.isZero()).isTrue();
            assertThat(nonZero.isZero()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when amounts are the same")
        void shouldBeEqualWhenAmountsAreTheSame() {
            Money money1 = new Money(10.50);
            Money money2 = new Money("10.50");
            Money money3 = new Money(new BigDecimal("10.50"));
            
            assertThat(money1).isEqualTo(money2);
            assertThat(money1).isEqualTo(money3);
            assertThat(money2).isEqualTo(money3);
        }

        @Test
        @DisplayName("Should not be equal when amounts are different")
        void shouldNotBeEqualWhenAmountsAreDifferent() {
            Money money1 = new Money(10.50);
            Money money2 = new Money(10.51);
            
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should have same hash code for equal amounts")
        void shouldHaveSameHashCodeForEqualAmounts() {
            Money money1 = new Money(10.50);
            Money money2 = new Money("10.50");
            
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("Should handle null and different types in equals")
        void shouldHandleNullAndDifferentTypesInEquals() {
            Money money = new Money(10.50);
            
            assertThat(money).isNotEqualTo(null);
            assertThat(money).isNotEqualTo("10.50");
            assertThat(money).isNotEqualTo(10.50);
        }
    }

    @Nested
    @DisplayName("Precision Tests")
    class PrecisionTests {

        @Test
        @DisplayName("Should handle floating point precision issues")
        void shouldHandleFloatingPointPrecisionIssues() {
            // Test that demonstrates why we use BigDecimal
            Money money1 = new Money(0.1);
            Money money2 = new Money(0.2);
            Money result = money1.add(money2);
            
            // This should be exactly 0.30, not 0.30000000000000004
            assertThat(result.toString()).isEqualTo("$0.30");
            assertThat(result.toDouble()).isEqualTo(0.30);
        }

        @Test
        @DisplayName("Should maintain precision in complex calculations")
        void shouldMaintainPrecisionInComplexCalculations() {
            Money start = new Money("1000.00");
            Money fee = new Money("0.03"); // 3 cents
            
            // Subtract fee 10 times
            Money result = start;
            for (int i = 0; i < 10; i++) {
                result = result.subtract(fee);
            }
            
            assertThat(result.toString()).isEqualTo("$999.70");
        }

        @Test
        @DisplayName("Should handle very small amounts")
        void shouldHandleVerySmallAmounts() {
            Money penny = new Money("0.01");
            Money twoPennies = penny.add(penny);
            
            assertThat(twoPennies.toString()).isEqualTo("$0.02");
            assertThat(twoPennies.toDouble()).isEqualTo(0.02);
        }
    }
}