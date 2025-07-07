package com.example.banking.api.service.process.operations;

import com.example.banking.api.model.BankingTransaction;
import com.example.banking.api.service.process.ProcessCommunication;
import com.example.banking.api.service.process.ProcessOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Session-aware transaction history operation that works with pre-authenticated processes.
 * This operation assumes the process is already authenticated and skips authentication.
 */
public class SessionTransactionHistoryOperation implements ProcessOperation<List<BankingTransaction>> {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionTransactionHistoryOperation.class);
    
    private final String username;
    
    // Pattern for parsing transaction entries
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
        "(Deposit|Withdrawal)\\s+of\\s+\\$([0-9]+\\.?[0-9]*)"  
    );
    
    public SessionTransactionHistoryOperation(String username) {
        this.username = username;
    }
    
    @Override
    public List<BankingTransaction> execute(ProcessCommunication communication) throws Exception {
        logger.info("=== SESSION TRANSACTION HISTORY OPERATION START ===");
        
        // For session-based operations, we assume the process is already authenticated
        // Send the transaction history command directly
        logger.info("Sending transaction history command (5)...");
        communication.sendCommand("5"); // Choose transaction history option from banking menu
        
        // Wait for transaction history output
        logger.info("Waiting for transaction history output...");
        String historyOutput = communication.readOutput(1000);
        logger.info("Transaction history output: [{}]", historyOutput);
        
        // Parse transactions from output
        List<BankingTransaction> transactions = parseTransactions(historyOutput);
        logger.info("Parsed {} transactions", transactions.size());
        
        logger.info("=== SESSION TRANSACTION HISTORY OPERATION END - Count: {} ===", transactions.size());
        return transactions;
    }
    
    /**
     * Parse transactions from process output.
     */
    private List<BankingTransaction> parseTransactions(String output) {
        List<BankingTransaction> transactions = new ArrayList<>();
        
        // Look for transaction patterns in the output
        Matcher matcher = TRANSACTION_PATTERN.matcher(output);
        while (matcher.find()) {
            try {
                String type = matcher.group(1);
                double amount = Double.parseDouble(matcher.group(2));
                
                BankingTransaction transaction = new BankingTransaction(
                    type,
                    amount,
                    LocalDateTime.now() // We don't have timestamp from the process, use current time
                );
                transactions.add(transaction);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse transaction amount from: {}", matcher.group(2), e);
            }
        }
        
        return transactions;
    }
}