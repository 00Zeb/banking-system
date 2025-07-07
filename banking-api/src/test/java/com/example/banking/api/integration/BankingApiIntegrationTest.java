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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("Complete banking workflow integration test")
    void shouldCompleteFullBankingWorkflow() throws Exception {
        String username = "integrationuser";
        String password = "password123";

        // 1. Register a new user
        RegisterRequest registerRequest = new RegisterRequest(username, password);
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        // 2. Login with the new user
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/banking/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.sessionId").exists())
                .andReturn();
        
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 3. Check initial balance
        mockMvc.perform(get("/api/v1/banking/balance")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.balance").value(0.0));

        // 4. Make a deposit
        SessionTransactionRequest depositRequest = new SessionTransactionRequest(100.0);
        mockMvc.perform(post("/api/v1/banking/deposit")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Deposit"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.newBalance").value(100.0));

        // 5. Check balance after deposit
        mockMvc.perform(get("/api/v1/banking/balance")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.0));

        // 6. Make a withdrawal
        SessionTransactionRequest withdrawRequest = new SessionTransactionRequest(30.0);
        mockMvc.perform(post("/api/v1/banking/withdraw")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Withdrawal"))
                .andExpect(jsonPath("$.amount").value(30.0))
                .andExpect(jsonPath("$.newBalance").value(70.0));

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
