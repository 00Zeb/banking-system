package com.example.banking.api.integration;

import com.example.banking.api.dto.LoginRequest;
import com.example.banking.api.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockHttpSession;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class SessionHandlingTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up data file before each test
        File dataFile = new File("banking_data.ser");
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }

    @Test
    public void testSessionHandling() throws Exception {
        String username = "sessionhandleuser";
        String password = "password123";

        // 1. Register user
        RegisterRequest registerRequest = new RegisterRequest(username, password);
        mockMvc.perform(post("/api/v1/banking/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Login and MANUALLY create session
        MockHttpSession session = new MockHttpSession();
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/banking/login")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. Use the SAME session instance for subsequent requests
        mockMvc.perform(get("/api/v1/banking/balance")
                .session(session))
                .andExpect(status().isOk());
    }
}