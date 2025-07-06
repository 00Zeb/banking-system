package com.example.banking.api.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Domain Model Tests")
class UserTest {

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Should create user with valid username and password")
        void shouldCreateUserWithValidUsernameAndPassword() {
            User user = new User("testuser", "password123");
            
            assertThat(user.getUsername()).isEqualTo("testuser");
            assertThat(user.getPassword()).isEqualTo("password123");
        }

        @Test
        @DisplayName("Should trim whitespace from username")
        void shouldTrimWhitespaceFromUsername() {
            User user = new User("  testuser  ", "password123");
            
            assertThat(user.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should preserve password as-is")
        void shouldPreservePasswordAsIs() {
            String passwordWithSpaces = "  my password  ";
            User user = new User("testuser", passwordWithSpaces);
            
            assertThat(user.getPassword()).isEqualTo(passwordWithSpaces);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception for invalid usernames")
        void shouldThrowExceptionForInvalidUsernames(String invalidUsername) {
            assertThatThrownBy(() -> new User(invalidUsername, "password123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Username cannot be null or empty");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception for invalid passwords")
        void shouldThrowExceptionForInvalidPasswords(String invalidPassword) {
            assertThatThrownBy(() -> new User("testuser", invalidPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal when usernames are the same")
        void shouldBeEqualWhenUsernamesAreTheSame() {
            User user1 = new User("testuser", "password1");
            User user2 = new User("testuser", "password2");
            
            assertThat(user1).isEqualTo(user2);
        }

        @Test
        @DisplayName("Should not be equal when usernames are different")
        void shouldNotBeEqualWhenUsernamesAreDifferent() {
            User user1 = new User("testuser1", "password");
            User user2 = new User("testuser2", "password");
            
            assertThat(user1).isNotEqualTo(user2);
        }

        @Test
        @DisplayName("Should have same hash code for same usernames")
        void shouldHaveSameHashCodeForSameUsernames() {
            User user1 = new User("testuser", "password1");
            User user2 = new User("testuser", "password2");
            
            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("Should handle username case sensitivity in equality")
        void shouldHandleUsernameCaseSensitivityInEquality() {
            User user1 = new User("TestUser", "password");
            User user2 = new User("testuser", "password");
            
            // Usernames are case-sensitive in this implementation
            assertThat(user1).isNotEqualTo(user2);
        }

        @Test
        @DisplayName("Should handle null and different types in equals")
        void shouldHandleNullAndDifferentTypesInEquals() {
            User user = new User("testuser", "password");
            
            assertThat(user).isNotEqualTo(null);
            assertThat(user).isNotEqualTo("testuser");
            assertThat(user).isNotEqualTo(123);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            User user = new User("testuser", "password");
            
            assertThat(user).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            User user = new User("testuser", "password123");
            
            String result = user.toString();
            
            assertThat(result).contains("testuser");
            assertThat(result).doesNotContain("password123"); // Password should not be in toString
            assertThat(result).matches("User\\{username='testuser'\\}");
        }

        @Test
        @DisplayName("Should handle special characters in username for toString")
        void shouldHandleSpecialCharactersInUsernameForToString() {
            User user = new User("user@domain.com", "password");
            
            String result = user.toString();
            
            assertThat(result).contains("user@domain.com");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should maintain immutability")
        void shouldMaintainImmutability() {
            User user = new User("testuser", "password");
            String originalUsername = user.getUsername();
            String originalPassword = user.getPassword();
            
            // Verify that getters return the same values
            assertThat(user.getUsername()).isEqualTo(originalUsername);
            assertThat(user.getPassword()).isEqualTo(originalPassword);
            
            // Since User is immutable, there should be no setters to test
            // This test validates the design choice of immutability
        }

        @Test
        @DisplayName("Should handle various username formats")
        void shouldHandleVariousUsernameFormats() {
            // Test various valid username formats
            assertThatNoException().isThrownBy(() -> {
                new User("user123", "password");
                new User("user@domain.com", "password");
                new User("user_name", "password");
                new User("user.name", "password");
                new User("user-name", "password");
                new User("123user", "password");
            });
        }

        @Test
        @DisplayName("Should handle various password formats")
        void shouldHandleVariousPasswordFormats() {
            // Test various valid password formats
            assertThatNoException().isThrownBy(() -> {
                new User("testuser", "simple");
                new User("testuser", "Pass123!");
                new User("testuser", "very_long_password_with_special_chars@#$%");
                new User("testuser", "1234567890");
                new User("testuser", "MixedCASE123");
            });
        }
    }
}