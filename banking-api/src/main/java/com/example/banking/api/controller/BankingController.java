package com.example.banking.api.controller;

import com.example.banking.api.dto.*;
import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.model.BankingUser;
import com.example.banking.api.service.BankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    public BankingController(BankingService bankingService) {
        this.bankingService = bankingService;
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
    @Operation(summary = "Register a new user", description = "Creates a new user account with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or username already exists"),
        @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    public ResponseEntity<com.example.banking.api.dto.ApiResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
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
    @Operation(summary = "Authenticate user", description = "Validates user credentials and returns user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        BankingUser user = bankingService.authenticateUser(request.getUsername(), request.getPassword());

        if (user != null) {
            UserResponse response = new UserResponse(user.getUsername(), user.getBalance());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money", description = "Deposits money into the user's account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid amount")
    })
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest request) {
        boolean success = bankingService.deposit(request.getUsername(), request.getPassword(), request.getAmount());

        if (success) {
            BankingUser user = bankingService.getAuthenticatedUser(request.getUsername(), request.getPassword());
            TransactionResponse response = new TransactionResponse(
                    "Deposit",
                    request.getAmount(),
                    java.time.LocalDateTime.now(),
                    user != null ? user.getBalance() : 0.0
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraws money from the user's account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Withdrawal successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds")
    })
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody TransactionRequest request) {
        boolean success = bankingService.withdraw(request.getUsername(), request.getPassword(), request.getAmount());

        if (success) {
            BankingUser user = bankingService.getAuthenticatedUser(request.getUsername(), request.getPassword());
            TransactionResponse response = new TransactionResponse(
                    "Withdrawal",
                    request.getAmount(),
                    java.time.LocalDateTime.now(),
                    user != null ? user.getBalance() : 0.0
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/balance")
    @Operation(summary = "Get account balance", description = "Retrieves the current account balance for the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<UserResponse> getBalance(@Valid @RequestBody LoginRequest request) {
        Double balance = bankingService.getBalance(request.getUsername(), request.getPassword());
        
        if (balance != null) {
            UserResponse response = new UserResponse(request.getUsername(), balance);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/transactions")
    @Operation(summary = "Get transaction history", description = "Retrieves the transaction history for the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<List<TransactionResponse>> getTransactions(@Valid @RequestBody LoginRequest request) {
        List<BankingTransaction> transactions = bankingService.getTransactions(request.getUsername(), request.getPassword());

        if (transactions != null) {
            List<TransactionResponse> response = transactions.stream()
                    .map(t -> new TransactionResponse(t.getType(), t.getAmount(), t.getTimestamp(), 0.0))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/account")
    @Operation(summary = "Delete user account", description = "Deletes the user account permanently")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<com.example.banking.api.dto.ApiResponse> deleteAccount(@Valid @RequestBody LoginRequest request) {
        boolean success = bankingService.deleteUser(request.getUsername(), request.getPassword());
        
        if (success) {
            return ResponseEntity.ok(com.example.banking.api.dto.ApiResponse.success("Account deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.example.banking.api.dto.ApiResponse.error("Invalid credentials"));
        }
    }
}
