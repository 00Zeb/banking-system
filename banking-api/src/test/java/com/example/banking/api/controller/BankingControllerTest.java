package com.example.banking.api.controller;

import com.example.banking.api.dto.LoginRequest;
import com.example.banking.api.dto.RegisterRequest;
import com.example.banking.api.dto.SessionTransactionRequest;
import com.example.banking.api.service.BankingService;
import com.example.banking.api.service.SessionBankingService;
import com.example.banking.api.service.session.SessionManager;
import com.example.banking.api.service.process.ProcessSessionManager;
import com.example.banking.api.model.BankingUser;
import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.domain.model.UserSession;
import com.example.banking.api.config.SessionInterceptor;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
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

    @MockBean
    private SessionBankingService sessionBankingService;

    @MockBean
    private SessionManager sessionManager;

    @MockBean
    private ProcessSessionManager processSessionManager;

    @MockBean
    private SessionInterceptor sessionInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    private BankingUser mockUser;

    @BeforeEach
    void setUp() throws Exception {
        mockUser = new BankingUser("testuser", 100.0);
        
        // Mock the SessionInterceptor to always allow requests
        when(sessionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
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
        @Disabled
        void shouldAuthenticateUserSuccessfully() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("testuser", "password");
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUsername()).thenReturn("testuser");
            when(mockSession.getSessionId()).thenReturn("session123");
            when(mockSession.getCreatedAt()).thenReturn(LocalDateTime.now());
            
            when(bankingService.authenticateUser("testuser", "password")).thenReturn(mockUser);
            when(sessionManager.createSession(eq("testuser"), any(HttpSession.class))).thenReturn(mockSession);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"));

            verify(bankingService).authenticateUser("testuser", "password");
            verify(sessionManager).createSession(eq("testuser"), any(HttpSession.class));
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
            SessionTransactionRequest request = new SessionTransactionRequest(100.0);
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUsername()).thenReturn("testuser");
            
            when(sessionBankingService.deposit(any(UserSession.class), eq(100.0))).thenReturn(true);
            when(sessionBankingService.getBalance(any(UserSession.class))).thenReturn(200.0);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .requestAttr("userSession", mockSession))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("Deposit"))
                    .andExpect(jsonPath("$.amount").value(100.0))
                    .andExpect(jsonPath("$.newBalance").value(200.0));

            verify(sessionBankingService).deposit(any(UserSession.class), eq(100.0));
            verify(sessionBankingService).getBalance(any(UserSession.class));
        }

        @Test
        @DisplayName("Should return unauthorized for missing session on deposit")
        void shouldReturnUnauthorizedForMissingSessionOnDeposit() throws Exception {
            // Given
            SessionTransactionRequest request = new SessionTransactionRequest(100.0);

            // When & Then - no session attribute set
            mockMvc.perform(post("/api/v1/banking/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should process withdrawal successfully")
        void shouldProcessWithdrawalSuccessfully() throws Exception {
            // Given
            SessionTransactionRequest request = new SessionTransactionRequest(50.0);
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUsername()).thenReturn("testuser");
            
            when(sessionBankingService.withdraw(any(UserSession.class), eq(50.0))).thenReturn(true);
            when(sessionBankingService.getBalance(any(UserSession.class))).thenReturn(100.0);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/withdraw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .requestAttr("userSession", mockSession))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("Withdrawal"))
                    .andExpect(jsonPath("$.amount").value(50.0))
                    .andExpect(jsonPath("$.newBalance").value(100.0));

            verify(sessionBankingService).withdraw(any(UserSession.class), eq(50.0));
            verify(sessionBankingService).getBalance(any(UserSession.class));
        }

        @Test
        @DisplayName("Should return bad request for insufficient funds")
        void shouldReturnBadRequestForInsufficientFunds() throws Exception {
            // Given
            SessionTransactionRequest request = new SessionTransactionRequest(1000.0);
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUsername()).thenReturn("testuser");
            
            when(sessionBankingService.withdraw(any(UserSession.class), eq(1000.0))).thenReturn(false);

            // When & Then
            mockMvc.perform(post("/api/v1/banking/withdraw")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .requestAttr("userSession", mockSession))
                    .andExpect(status().isBadRequest());

            verify(sessionBankingService).withdraw(any(UserSession.class), eq(1000.0));
        }
    }

    @Nested
    @DisplayName("Balance Tests")
    class BalanceTests {

        @Test
        @DisplayName("Should get balance successfully")
        void shouldGetBalanceSuccessfully() throws Exception {
            // Given
            UserSession mockSession = mock(UserSession.class);
            when(mockSession.getUsername()).thenReturn("testuser");
            
            when(sessionBankingService.getBalance(any(UserSession.class))).thenReturn(150.0);

            // When & Then
            mockMvc.perform(get("/api/v1/banking/balance")
                    .requestAttr("userSession", mockSession))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.balance").value(150.0));

            verify(sessionBankingService).getBalance(any(UserSession.class));
        }

        @Test
        @DisplayName("Should return unauthorized for missing session on balance check")
        void shouldReturnUnauthorizedForMissingSessionOnBalanceCheck() throws Exception {
            // When & Then - no session attribute set
            mockMvc.perform(get("/api/v1/banking/balance"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
