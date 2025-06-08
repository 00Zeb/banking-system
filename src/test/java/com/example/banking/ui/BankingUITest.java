package com.example.banking.ui;

import com.example.banking.domain.Account;
import com.example.banking.user.User;
import com.example.banking.user.UserManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankingUI Tests")
class BankingUITest {

    @Mock
    private UserManager mockUserManager;

    @Mock
    private User mockUser;

    @Mock
    private Account mockAccount;

    private BankingUI bankingUI;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        // Capture System.out for testing
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        bankingUI = new BankingUI(mockUserManager);
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create BankingUI with UserManager")
        void shouldCreateBankingUIWithUserManager() {
            // When
            BankingUI ui = new BankingUI(mockUserManager);

            // Then
            assertThat(ui).isNotNull();
        }

        @Test
        @DisplayName("Should handle null UserManager gracefully")
        void shouldHandleNullUserManagerGracefully() {
            // When/Then - Should not throw exception
            assertThatCode(() -> new BankingUI(null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Authentication Menu Tests")
    class AuthenticationMenuTests {

        @Test
        @DisplayName("Should display authentication menu")
        void shouldDisplayAuthenticationMenu() {
            // Given
            String input = "3\n"; // Exit option
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("===== Banking System =====");
            assertThat(output).contains("1. Login");
            assertThat(output).contains("2. Register");
            assertThat(output).contains("3. Exit");
            assertThat(output).contains("Thank you for using the Banking System!");
        }

        @Test
        @DisplayName("Should handle successful login")
        void shouldHandleSuccessfulLogin() {
            // Given
            String input = "1\ntestuser\npassword\n4\n3\n"; // Login, then logout, then exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            when(mockUser.getUsername()).thenReturn("testuser");
            when(mockUser.getAccount()).thenReturn(mockAccount);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Welcome, testuser!");
            assertThat(output).contains("Logged out successfully.");
            verify(mockUserManager).authenticateUser("testuser", "password");
        }

        @Test
        @DisplayName("Should handle failed login")
        void shouldHandleFailedLogin() {
            // Given
            String input = "1\nwronguser\nwrongpass\n3\n"; // Failed login, then exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("wronguser", "wrongpass")).thenReturn(null);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Authentication failed. Invalid username or password.");
            verify(mockUserManager).authenticateUser("wronguser", "wrongpass");
        }

        @Test
        @DisplayName("Should handle successful registration")
        void shouldHandleSuccessfulRegistration() {
            // Given
            String input = "2\nnewuser\nnewpass\n3\n"; // Register, then exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.registerUser("newuser", "newpass")).thenReturn(true);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Registration successful! You can now login.");
            verify(mockUserManager).registerUser("newuser", "newpass");
        }

        @Test
        @DisplayName("Should handle failed registration")
        void shouldHandleFailedRegistration() {
            // Given
            String input = "2\nexistinguser\npassword\n3\n"; // Failed registration, then exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.registerUser("existinguser", "password")).thenReturn(false);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Username already exists. Please choose another one.");
            verify(mockUserManager).registerUser("existinguser", "password");
        }

        @Test
        @DisplayName("Should handle invalid menu option")
        void shouldHandleInvalidMenuOption() {
            // Given
            String input = "99\n3\n"; // Invalid option, then exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Invalid option. Please try again.");
        }

        @Test
        @DisplayName("Should handle non-numeric input")
        void shouldHandleNonNumericInput() {
            // Given
            String input = "abc\n3\n"; // Non-numeric input, then exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Invalid input. Please enter a number.");
        }
    }

    @Nested
    @DisplayName("Banking Menu Tests")
    class BankingMenuTests {

        @BeforeEach
        void setUpLoggedInUser() {
            when(mockUser.getUsername()).thenReturn("testuser");
            when(mockUser.getAccount()).thenReturn(mockAccount);
        }

        @Test
        @DisplayName("Should display banking menu for logged in user")
        void shouldDisplayBankingMenuForLoggedInUser() {
            // Given
            String input = "1\ntestuser\npassword\n4\n3\n"; // Login, then logout, then exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Welcome to Simple Banking App - Logged in as: testuser");
            assertThat(output).contains("1. Deposit");
            assertThat(output).contains("2. Withdraw");
            assertThat(output).contains("3. List Transactions");
            assertThat(output).contains("4. Logout");
            assertThat(output).contains("5. Exit Application");
        }

        @Test
        @DisplayName("Should handle deposit operation")
        void shouldHandleDepositOperation() {
            // Given
            String input = "1\ntestuser\npassword\n1\n100.50\n4\n3\n"; // Login, deposit, logout, exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            verify(mockAccount).deposit(100.50);
        }

        @Test
        @DisplayName("Should handle withdrawal operation")
        void shouldHandleWithdrawalOperation() {
            // Given
            String input = "1\ntestuser\npassword\n2\n50.25\n4\n3\n"; // Login, withdraw, logout, exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            verify(mockAccount).withdraw(50.25);
        }

        @Test
        @DisplayName("Should handle list transactions operation")
        void shouldHandleListTransactionsOperation() {
            // Given
            String input = "1\ntestuser\npassword\n3\n4\n3\n"; // Login, list transactions, logout, exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            verify(mockAccount).listTransactions();
        }

        @Test
        @DisplayName("Should handle negative deposit amount")
        void shouldHandleNegativeDepositAmount() {
            // Given
            String input = "1\ntestuser\npassword\n1\n-50\n4\n3\n"; // Login, negative deposit, logout, exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Deposit amount must be positive.");
            verify(mockAccount, never()).deposit(anyDouble());
        }

        @Test
        @DisplayName("Should handle negative withdrawal amount")
        void shouldHandleNegativeWithdrawalAmount() {
            // Given
            String input = "1\ntestuser\npassword\n2\n-25\n4\n3\n"; // Login, negative withdraw, logout, exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Withdrawal amount must be positive.");
            verify(mockAccount, never()).withdraw(anyDouble());
        }

        @Test
        @DisplayName("Should handle invalid amount input")
        void shouldHandleInvalidAmountInput() {
            // Given
            String input = "1\ntestuser\npassword\n1\nabc\n4\n3\n"; // Login, invalid amount, logout, exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Invalid amount. Please enter a number.");
            verify(mockAccount, never()).deposit(anyDouble());
        }

        @Test
        @DisplayName("Should handle logout operation")
        void shouldHandleLogoutOperation() {
            // Given
            String input = "1\ntestuser\npassword\n4\n3\n"; // Login, logout, exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            when(mockUserManager.authenticateUser("testuser", "password")).thenReturn(mockUser);
            
            BankingUI ui = new BankingUI(mockUserManager);

            // When
            ui.start();

            // Then
            String output = outputStream.toString();
            assertThat(output).contains("Logged out successfully.");
        }
    }
}
