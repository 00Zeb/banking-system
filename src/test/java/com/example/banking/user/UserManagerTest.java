package com.example.banking.user;

import com.example.banking.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserManager Tests")
class UserManagerTest {

    @Mock
    private UserRepository mockRepository;

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        // Reset the mock before each test
        reset(mockRepository);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create UserManager with default users when no repository provided")
        void shouldCreateUserManagerWithDefaultUsers() {
            // When
            userManager = new UserManager();

            // Then
            assertThat(userManager.authenticateUser("admin", "admin123")).isNotNull();
            assertThat(userManager.authenticateUser("john", "pass123")).isNotNull();
        }

        @Test
        @DisplayName("Should create UserManager with repository and load existing users")
        void shouldCreateUserManagerWithRepositoryAndLoadUsers() {
            // Given
            User existingUser = new User("existing", "password");
            when(mockRepository.getAllUsers()).thenReturn(Arrays.asList(existingUser));

            // When
            userManager = new UserManager(mockRepository);

            // Then
            verify(mockRepository).getAllUsers();
            assertThat(userManager.authenticateUser("existing", "password")).isNotNull();
        }

        @Test
        @DisplayName("Should create UserManager with empty repository and no default users")
        void shouldCreateUserManagerWithEmptyRepository() {
            // Given
            when(mockRepository.getAllUsers()).thenReturn(Collections.emptyList());

            // When
            userManager = new UserManager(mockRepository);

            // Then
            verify(mockRepository).getAllUsers();
            // When repository is empty, UserManager does NOT add default users (only when repository is null)
            assertThat(userManager.authenticateUser("admin", "admin123")).isNull();
            assertThat(userManager.authenticateUser("john", "pass123")).isNull();
        }

        @Test
        @DisplayName("Should handle null repository gracefully")
        void shouldHandleNullRepository() {
            // When
            userManager = new UserManager(null);

            // Then
            assertThat(userManager.authenticateUser("admin", "admin123")).isNotNull();
            assertThat(userManager.authenticateUser("john", "pass123")).isNotNull();
        }
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @BeforeEach
        void setUp() {
            when(mockRepository.getAllUsers()).thenReturn(Collections.emptyList());
            userManager = new UserManager(mockRepository);
        }

        @Test
        @DisplayName("Should successfully register new user")
        void shouldRegisterNewUser() {
            // When
            boolean result = userManager.registerUser("newuser", "password123");

            // Then
            assertThat(result).isTrue();
            verify(mockRepository).saveUser(any(User.class));
            assertThat(userManager.authenticateUser("newuser", "password123")).isNotNull();
        }

        @Test
        @DisplayName("Should fail to register user with existing username")
        void shouldFailToRegisterExistingUser() {
            // Given
            userManager.registerUser("existinguser", "password123");
            reset(mockRepository); // Reset to clear previous interactions

            // When
            boolean result = userManager.registerUser("existinguser", "newpassword");

            // Then
            assertThat(result).isFalse();
            verify(mockRepository, never()).saveUser(any(User.class));
        }

        @Test
        @DisplayName("Should register user without repository")
        void shouldRegisterUserWithoutRepository() {
            // Given
            userManager = new UserManager();

            // When
            boolean result = userManager.registerUser("testuser", "testpass");

            // Then
            assertThat(result).isTrue();
            assertThat(userManager.authenticateUser("testuser", "testpass")).isNotNull();
        }
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {

        @BeforeEach
        void setUp() {
            when(mockRepository.getAllUsers()).thenReturn(Collections.emptyList());
            userManager = new UserManager(mockRepository);
            userManager.registerUser("testuser", "testpass");
        }

        @Test
        @DisplayName("Should authenticate user with correct credentials")
        void shouldAuthenticateUserWithCorrectCredentials() {
            // When
            User authenticatedUser = userManager.authenticateUser("testuser", "testpass");

            // Then
            assertThat(authenticatedUser).isNotNull();
            assertThat(authenticatedUser.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should fail authentication with wrong password")
        void shouldFailAuthenticationWithWrongPassword() {
            // When
            User authenticatedUser = userManager.authenticateUser("testuser", "wrongpass");

            // Then
            assertThat(authenticatedUser).isNull();
        }

        @Test
        @DisplayName("Should fail authentication with non-existent user")
        void shouldFailAuthenticationWithNonExistentUser() {
            // When
            User authenticatedUser = userManager.authenticateUser("nonexistent", "password");

            // Then
            assertThat(authenticatedUser).isNull();
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {

        @BeforeEach
        void setUp() {
            when(mockRepository.getAllUsers()).thenReturn(Collections.emptyList());
            userManager = new UserManager(mockRepository);
            userManager.registerUser("userToDelete", "password");
            reset(mockRepository); // Reset to clear registration interactions
        }

        @Test
        @DisplayName("Should successfully delete existing user")
        void shouldDeleteExistingUser() {
            // When
            boolean result = userManager.deleteUser("userToDelete");

            // Then
            assertThat(result).isTrue();
            verify(mockRepository).deleteUser("userToDelete");
            assertThat(userManager.authenticateUser("userToDelete", "password")).isNull();
        }

        @Test
        @DisplayName("Should fail to delete non-existent user")
        void shouldFailToDeleteNonExistentUser() {
            // When
            boolean result = userManager.deleteUser("nonexistent");

            // Then
            assertThat(result).isFalse();
            verify(mockRepository, never()).deleteUser(anyString());
        }

        @Test
        @DisplayName("Should delete user without repository")
        void shouldDeleteUserWithoutRepository() {
            // Given
            userManager = new UserManager();

            // When
            boolean result = userManager.deleteUser("admin");

            // Then
            assertThat(result).isTrue();
            assertThat(userManager.authenticateUser("admin", "admin123")).isNull();
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UserUpdateTests {

        private User testUser;

        @BeforeEach
        void setUp() {
            when(mockRepository.getAllUsers()).thenReturn(Collections.emptyList());
            userManager = new UserManager(mockRepository);
            userManager.registerUser("testuser", "password");
            testUser = userManager.authenticateUser("testuser", "password");
            reset(mockRepository); // Reset to clear registration interactions
        }

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateExistingUser() {
            // When
            userManager.updateUser(testUser);

            // Then
            verify(mockRepository).updateUser(testUser);
        }

        @Test
        @DisplayName("Should not update user without repository")
        void shouldNotUpdateUserWithoutRepository() {
            // Given
            userManager = new UserManager();
            User user = new User("test", "pass");

            // When
            userManager.updateUser(user);

            // Then - No exception should be thrown
            // This test verifies the method handles null repository gracefully
        }
    }

    @Nested
    @DisplayName("Save All Users Tests")
    class SaveAllUsersTests {

        @BeforeEach
        void setUp() {
            when(mockRepository.getAllUsers()).thenReturn(Collections.emptyList());
            userManager = new UserManager(mockRepository);
        }

        @Test
        @DisplayName("Should save all users when repository is available")
        void shouldSaveAllUsersWithRepository() {
            // When
            userManager.saveAllUsers();

            // Then
            verify(mockRepository).saveAllUsers();
        }

        @Test
        @DisplayName("Should handle save all users without repository")
        void shouldHandleSaveAllUsersWithoutRepository() {
            // Given
            userManager = new UserManager();

            // When
            userManager.saveAllUsers();

            // Then - No exception should be thrown
            // This test verifies the method handles null repository gracefully
        }
    }
}
