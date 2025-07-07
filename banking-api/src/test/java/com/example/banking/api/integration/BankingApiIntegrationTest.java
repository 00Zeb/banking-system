package com.example.banking.api.integration;

import com.example.banking.api.dto.LoginRequest;
import com.example.banking.api.dto.RegisterRequest;
import com.example.banking.api.dto.SessionTransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockHttpSession;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "logging.level.com.example.banking.api=DEBUG"
})
@DisplayName("Banking API Integration Tests")
class BankingApiIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Configure MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean up data file before each test
        File dataFile = new File("banking_data.ser");
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }

    @AfterEach
    void tearDown() {
        // Clean up data file after each test
        File dataFile = new File("banking_data.ser");
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }

    @Test
    @DisplayName("Complete banking workflow integration test with performance metrics")
    void shouldCompleteFullBankingWorkflow() throws Exception {
        // Performance tracking setup
        Instant startTime = Instant.now();
        long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        String username = "integrationuser";
        String password = "password123";
        
        // Performance thresholds (in milliseconds)
        final long MAX_REGISTRATION_TIME = 2000;
        final long MAX_LOGIN_TIME = 1500;
        final long MAX_TRANSACTION_TIME = 1000;
        final long MAX_BALANCE_CHECK_TIME = 500;
        final long MAX_TOTAL_WORKFLOW_TIME = 10000;

        // 1. Register a new user - with performance tracking
        Instant regStart = Instant.now();
        RegisterRequest registerRequest = new RegisterRequest(username, password);
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
        
        long registrationTime = Duration.between(regStart, Instant.now()).toMillis();
        assertTrue(registrationTime < MAX_REGISTRATION_TIME, 
                "Registration took " + registrationTime + "ms, exceeds threshold of " + MAX_REGISTRATION_TIME + "ms");

        // 2. Login with the new user and create session - with performance tracking
        Instant loginStart = Instant.now();
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/banking/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.sessionId").exists())
                .andReturn();
        
        long loginTime = Duration.between(loginStart, Instant.now()).toMillis();
        assertTrue(loginTime < MAX_LOGIN_TIME, 
                "Login took " + loginTime + "ms, exceeds threshold of " + MAX_LOGIN_TIME + "ms");
                
        // Get the session created by the login request
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 3. Check initial balance
        mockMvc.perform(get("/api/v1/banking/balance")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.balance").value(0.0));

        // 4. Make a deposit - with performance tracking
        Instant depositStart = Instant.now();
        SessionTransactionRequest depositRequest = new SessionTransactionRequest(100.0);
        mockMvc.perform(post("/api/v1/banking/deposit")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Deposit"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.newBalance").value(100.0));
        
        long depositTime = Duration.between(depositStart, Instant.now()).toMillis();
        assertTrue(depositTime < MAX_TRANSACTION_TIME, 
                "Deposit took " + depositTime + "ms, exceeds threshold of " + MAX_TRANSACTION_TIME + "ms");

        // 5. Check balance after deposit
        mockMvc.perform(get("/api/v1/banking/balance")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.0));

        // 6. Make a withdrawal - with performance tracking
        Instant withdrawStart = Instant.now();
        SessionTransactionRequest withdrawRequest = new SessionTransactionRequest(30.0);
        mockMvc.perform(post("/api/v1/banking/withdraw")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Withdrawal"))
                .andExpect(jsonPath("$.amount").value(30.0))
                .andExpect(jsonPath("$.newBalance").value(70.0));
        
        long withdrawTime = Duration.between(withdrawStart, Instant.now()).toMillis();
        assertTrue(withdrawTime < MAX_TRANSACTION_TIME, 
                "Withdrawal took " + withdrawTime + "ms, exceeds threshold of " + MAX_TRANSACTION_TIME + "ms");

        // 7. Check final balance
        mockMvc.perform(get("/api/v1/banking/balance")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70.0));

        // 8. Get transaction history
        mockMvc.perform(get("/api/v1/banking/transactions")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("Deposit"))
                .andExpect(jsonPath("$[1].type").value("Withdrawal"));

        // 9. Try withdrawal with insufficient funds
        SessionTransactionRequest largeWithdrawRequest = new SessionTransactionRequest(100.0);
        mockMvc.perform(post("/api/v1/banking/withdraw")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeWithdrawRequest)))
                .andExpect(status().isBadRequest());

        // 10. Verify balance unchanged after failed withdrawal
        mockMvc.perform(get("/api/v1/banking/balance")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70.0));
        
        // 11. Logout
        mockMvc.perform(post("/api/v1/banking/logout")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Session ended successfully"));
        
        // Performance summary
        long totalTime = Duration.between(startTime, Instant.now()).toMillis();
        long finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        // Performance assertions
        assertTrue(totalTime < MAX_TOTAL_WORKFLOW_TIME, 
                "Total workflow took " + totalTime + "ms, exceeds threshold of " + MAX_TOTAL_WORKFLOW_TIME + "ms");
        
        // Log performance metrics for monitoring
        System.out.println("=== Performance Metrics ===");
        System.out.println("Registration: " + registrationTime + "ms");
        System.out.println("Login: " + loginTime + "ms");
        System.out.println("Deposit: " + depositTime + "ms");
        System.out.println("Withdrawal: " + withdrawTime + "ms");
        System.out.println("Total Workflow: " + totalTime + "ms");
        System.out.println("Memory Used: " + (memoryUsed / 1024) + " KB");
        System.out.println("============================");
    }

    @Test
    @DisplayName("Should handle concurrent users effectively")
    @Disabled("MockMvc is not thread-safe - needs refactor for concurrent testing")
    void shouldHandleConcurrentUsers() throws Exception {
        final int CONCURRENT_USERS = 5;
        final int TRANSACTIONS_PER_USER = 3;
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        Instant startTime = Instant.now();
        
        // Create concurrent user workflows
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String username = "concurrentuser" + userId;
                    String password = "password123";
                    
                    // Register user
                    RegisterRequest registerRequest = new RegisterRequest(username, password);
                    mockMvc.perform(post("/api/v1/banking/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                            .andExpect(status().isCreated());
                    
                    // Login
                    LoginRequest loginRequest = new LoginRequest(username, password);
                    MvcResult loginResult = mockMvc.perform(post("/api/v1/banking/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                            .andExpect(status().isOk())
                            .andReturn();
                    
                    MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
                    
                    // Perform multiple transactions
                    for (int t = 0; t < TRANSACTIONS_PER_USER; t++) {
                        // Deposit
                        SessionTransactionRequest depositRequest = new SessionTransactionRequest(50.0);
                        mockMvc.perform(post("/api/v1/banking/deposit")
                                .session(session)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(depositRequest)))
                                .andExpect(status().isOk());
                        
                        // Check balance
                        mockMvc.perform(get("/api/v1/banking/balance")
                                .session(session))
                                .andExpect(status().isOk());
                    }
                    
                    // Logout
                    mockMvc.perform(post("/api/v1/banking/logout")
                            .session(session))
                            .andExpect(status().isOk());
                            
                } catch (Exception e) {
                    throw new RuntimeException("Concurrent user test failed for user " + userId, e);
                }
            }, executor);
            
            futures.add(future);
        }
        
        // Wait for all concurrent operations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        
        long totalTime = Duration.between(startTime, Instant.now()).toMillis();
        long averageTimePerUser = totalTime / CONCURRENT_USERS;
        
        // Performance assertions for concurrent execution
        assertTrue(totalTime < 15000, 
                "Concurrent execution took " + totalTime + "ms, should complete within 15 seconds");
        assertTrue(averageTimePerUser < 5000, 
                "Average time per user was " + averageTimePerUser + "ms, should be under 5 seconds");
        
        System.out.println("=== Concurrent User Performance ==");
        System.out.println("Users: " + CONCURRENT_USERS);
        System.out.println("Transactions per user: " + TRANSACTIONS_PER_USER);
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Average time per user: " + averageTimePerUser + "ms");
        System.out.println("=================================");
        
        executor.shutdown();
    }

    @Test
    @DisplayName("Should meet performance thresholds under load")
    void shouldMeetPerformanceThresholds() throws Exception {
        final int LOAD_ITERATIONS = 10;
        List<Long> operationTimes = new ArrayList<>();
        
        String username = "perfuser";
        String password = "password123";
        
        // Setup user
        RegisterRequest registerRequest = new RegisterRequest(username, password);
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/banking/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();
        
        // Test multiple iterations of operations
        for (int i = 0; i < LOAD_ITERATIONS; i++) {
            // Test deposit performance
            Instant start = Instant.now();
            SessionTransactionRequest depositRequest = new SessionTransactionRequest(10.0);
            mockMvc.perform(post("/api/v1/banking/deposit")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(depositRequest)))
                    .andExpect(status().isOk());
            long depositTime = Duration.between(start, Instant.now()).toMillis();
            operationTimes.add(depositTime);
            
            // Test balance check performance
            start = Instant.now();
            mockMvc.perform(get("/api/v1/banking/balance")
                    .session(session))
                    .andExpect(status().isOk());
            long balanceTime = Duration.between(start, Instant.now()).toMillis();
            operationTimes.add(balanceTime);
            
            // Test transaction history performance
            start = Instant.now();
            mockMvc.perform(get("/api/v1/banking/transactions")
                    .session(session))
                    .andExpect(status().isOk());
            long historyTime = Duration.between(start, Instant.now()).toMillis();
            operationTimes.add(historyTime);
        }
        
        // Calculate statistics
        double averageTime = operationTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxTime = operationTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        long minTime = operationTimes.stream().mapToLong(Long::longValue).min().orElse(0L);
        
        // Performance assertions
        assertTrue(averageTime < 500, 
                "Average operation time was " + averageTime + "ms, should be under 500ms");
        assertTrue(maxTime < 1000, 
                "Maximum operation time was " + maxTime + "ms, should be under 1000ms");
        
        // Cleanup
        mockMvc.perform(post("/api/v1/banking/logout")
                .session(session))
                .andExpect(status().isOk());
        
        System.out.println("=== Load Test Performance ===");
        System.out.println("Iterations: " + LOAD_ITERATIONS);
        System.out.println("Total operations: " + operationTimes.size());
        System.out.println("Average time: " + String.format("%.2f", averageTime) + "ms");
        System.out.println("Min time: " + minTime + "ms");
        System.out.println("Max time: " + maxTime + "ms");
        System.out.println("=============================");
    }

    @Test
    @DisplayName("Should handle authentication errors correctly")
    void shouldHandleAuthenticationErrorsCorrectly() throws Exception {
        String username = "testuser";
        String password = "password123";
        String wrongPassword = "wrongpassword";

        // 1. Register user
        RegisterRequest registerRequest = new RegisterRequest(username, password);
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Try login with wrong password
        LoginRequest wrongLoginRequest = new LoginRequest(username, wrongPassword);
        mockMvc.perform(post("/api/v1/banking/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                .andExpect(status().isUnauthorized());

        // 3. Try deposit without session
        SessionTransactionRequest depositRequest = new SessionTransactionRequest(50.0);
        mockMvc.perform(post("/api/v1/banking/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isUnauthorized());

        // 4. Try balance check without session
        mockMvc.perform(get("/api/v1/banking/balance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle validation errors correctly")
    void shouldHandleValidationErrorsCorrectly() throws Exception {
        // 1. Try to register with invalid data
        RegisterRequest invalidRegisterRequest = new RegisterRequest("", ""); // Empty fields
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 2. Try to register with short password
        RegisterRequest shortPasswordRequest = new RegisterRequest("validuser", "123"); // Too short
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Try deposit with negative amount (without session to get validation error)
        SessionTransactionRequest negativeDepositRequest = new SessionTransactionRequest(-50.0);
        mockMvc.perform(post("/api/v1/banking/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeDepositRequest)))
                .andExpect(status().isUnauthorized()); // Will get 401 due to session validation first
    }

    @Test
    @DisplayName("Should handle duplicate registration correctly")
    void shouldHandleDuplicateRegistrationCorrectly() throws Exception {
        String username = "duplicateuser";
        String password = "password123";

        // 1. Register user first time
        RegisterRequest registerRequest = new RegisterRequest(username, password);
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Try to register same user again
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }
}
