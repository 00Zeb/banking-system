package com.example.banking.api.controller;

import com.example.banking.api.dto.*;
import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import com.example.banking.api.service.BankingService;
import com.example.banking.api.service.SessionBankingService;
import com.example.banking.api.service.session.SessionManager;
import com.example.banking.api.service.process.ProcessSessionManager;
import com.example.banking.api.domain.model.UserSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * REST Controller for Banking operations.
 * This controller delegates all operations to the existing banking core application.
 */
@RestController
@RequestMapping("/api/v1/banking")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8081", "http://127.0.0.1:8081"})
@Tag(name = "Banking API", description = "REST API for banking operations")
public class BankingController {

    private final BankingService bankingService;
    private final SessionBankingService sessionBankingService;
    private final SessionManager sessionManager;
    private final ProcessSessionManager processSessionManager;

    @Autowired
    public BankingController(BankingService bankingService, SessionBankingService sessionBankingService, SessionManager sessionManager, ProcessSessionManager processSessionManager) {
        this.bankingService = bankingService;
        this.sessionBankingService = sessionBankingService;
        this.sessionManager = sessionManager;
        this.processSessionManager = processSessionManager;
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Simple health check endpoint for CORS testing")
    public ResponseEntity<com.example.banking.api.dto.ApiResponse> health() {
        return ResponseEntity.ok(com.example.banking.api.dto.ApiResponse.success("Banking API is running"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with the provided credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    public ResponseEntity<com.example.banking.api.dto.ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        boolean success = bankingService.registerUser(request.getUsername(), request.getPassword());

        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(com.example.banking.api.dto.ApiResponse.success("User registered successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(com.example.banking.api.dto.ApiResponse.error("Username already exists"));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and create session", description = "Validates user credentials, creates a session, and returns user information with session details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful, session created"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<SessionResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // Create HTTP session first
        HttpSession httpSession = httpRequest.getSession(true);

        // Create user session
        UserSession userSession = sessionManager.createSession(request.getUsername(), httpSession);

        // Authenticate user and create session process
        BankingUser user = sessionBankingService.authenticateAndStartSession(userSession, request.getPassword());

        if (user != null) {
            // Calculate session expiration
            LocalDateTime expiresAt = userSession.getCreatedAt().plusSeconds(1800); // 30 minutes

            SessionResponse response = new SessionResponse(
                user.getUsername(),
                user.getBalance(),
                userSession.getSessionId(),
                userSession.getCreatedAt(),
                expiresAt
            );

            return ResponseEntity.ok(response);
        } else {
            // Authentication failed, clean up the session
            sessionManager.invalidateSession(httpSession);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "End user session", description = "Terminates the current user session and associated process")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session ended successfully"),
        @ApiResponse(responseCode = "401", description = "No active session found")
    })
    public ResponseEntity<com.example.banking.api.dto.ApiResponse> logout(HttpServletRequest httpRequest) {
        HttpSession httpSession = httpRequest.getSession(false);
        if (httpSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.example.banking.api.dto.ApiResponse.error("No active session found"));
        }
        
        Optional<UserSession> userSessionOpt = sessionManager.getSession(httpSession);
        if (userSessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.example.banking.api.dto.ApiResponse.error("No valid session found"));
        }
        
        UserSession userSession = userSessionOpt.get();
        
        // Terminate the session process
        processSessionManager.terminateSessionProcess(userSession);

        // Remove session from session manager (this also invalidates the HTTP session)
        sessionManager.invalidateSession(httpSession);
        
        return ResponseEntity.ok(com.example.banking.api.dto.ApiResponse.success("Session ended successfully"));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money", description = "Deposits the specified amount to the user's account using session-based authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit successful"),
        @ApiResponse(responseCode = "401", description = "No valid session found"),
        @ApiResponse(responseCode = "400", description = "Invalid amount")
    })
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody SessionTransactionRequest request, HttpServletRequest httpRequest) {
        // Get user session from request attributes (set by SessionInterceptor)
        UserSession userSession = (UserSession) httpRequest.getAttribute("userSession");
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean success = sessionBankingService.deposit(userSession, request.getAmount());

        if (success) {
            Double balance = sessionBankingService.getBalance(userSession);
            TransactionResponse response = new TransactionResponse(
                    "Deposit",
                    request.getAmount(),
                    java.time.LocalDateTime.now(),
                    balance != null ? balance : 0.0
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraws the specified amount from the user's account using session-based authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Withdrawal successful"),
        @ApiResponse(responseCode = "401", description = "No valid session found"),
        @ApiResponse(responseCode = "400", description = "Insufficient funds or invalid amount")
    })
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody SessionTransactionRequest request, HttpServletRequest httpRequest) {
        // Get user session from request attributes (set by SessionInterceptor)
        UserSession userSession = (UserSession) httpRequest.getAttribute("userSession");
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean success = sessionBankingService.withdraw(userSession, request.getAmount());

        if (success) {
            Double balance = sessionBankingService.getBalance(userSession);
            TransactionResponse response = new TransactionResponse(
                    "Withdrawal",
                    request.getAmount(),
                    java.time.LocalDateTime.now(),
                    balance != null ? balance : 0.0
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/balance")
    @Operation(summary = "Get account balance", description = "Retrieves the current account balance for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "No valid session found")
    })
    public ResponseEntity<UserResponse> getBalance(HttpServletRequest httpRequest) {
        // Get user session from request attributes (set by SessionInterceptor)
        UserSession userSession = (UserSession) httpRequest.getAttribute("userSession");
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Double balance = sessionBankingService.getBalance(userSession);
        
        if (balance != null) {
            UserResponse response = new UserResponse(userSession.getUsername(), balance);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history", description = "Retrieves the transaction history for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "No valid session found")
    })
    public ResponseEntity<List<TransactionResponse>> getTransactions(HttpServletRequest httpRequest) {
        // Get user session from request attributes (set by SessionInterceptor)
        UserSession userSession = (UserSession) httpRequest.getAttribute("userSession");
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<BankingTransaction> transactions = sessionBankingService.getTransactions(userSession);

        if (transactions != null) {
            List<TransactionResponse> response = transactions.stream()
                    .map(t -> new TransactionResponse(t.getType(), t.getAmount(), t.getTimestamp(), 0.0))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/user")
    @Operation(summary = "Delete user account", description = "Deletes the authenticated user account and all associated data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "401", description = "No valid session found")
    })
    public ResponseEntity<com.example.banking.api.dto.ApiResponse> deleteUser(HttpServletRequest httpRequest) {
        // Get user session from request attributes (set by SessionInterceptor)
        UserSession userSession = (UserSession) httpRequest.getAttribute("userSession");
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.example.banking.api.dto.ApiResponse.error("No valid session found"));
        }
        
        boolean success = sessionBankingService.deleteUser(userSession);

        if (success) {
            // Terminate the session process
            processSessionManager.terminateSessionProcess(userSession);
            
            // Remove session from session manager
            HttpSession httpSession = httpRequest.getSession(false);
            if (httpSession != null) {
                sessionManager.invalidateSession(httpSession);
            }
            
            return ResponseEntity.ok(com.example.banking.api.dto.ApiResponse.success("Account deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.example.banking.api.dto.ApiResponse.error("Failed to delete account"));
        }
    }
}
