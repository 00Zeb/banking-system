package com.example.banking.ui;

import com.example.banking.Account;
import com.example.banking.User;
import com.example.banking.UserManager;

import java.util.Scanner;

/**
 * Handles all user interface concerns for the banking application.
 */
public class BankingUI {
    private Scanner scanner;
    private UserManager userManager;
    private User currentUser;

    public BankingUI(UserManager userManager) {
        this.scanner = new Scanner(System.in);
        this.userManager = userManager;
        this.currentUser = null;
    }

    public void start() {
        boolean exitApp = false;
        
        while (!exitApp) {
            if (currentUser == null) {
                exitApp = showAuthMenu();
            } else {
                showBankingMenu();
            }
        }
        
        scanner.close();
        System.out.println("Thank you for using the Banking System!");
    }

    private boolean showAuthMenu() {
        System.out.println("\n===== Banking System =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        
        int authChoice = readIntInput();
        if (authChoice == -1) return false;
        
        switch (authChoice) {
            case 1: handleLogin(); break;
            case 2: handleRegistration(); break;
            case 3: return true; // Exit application
            default: System.out.println("Invalid option. Please try again.");
        }
        return false;
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        currentUser = userManager.authenticateUser(username, password);
        if (currentUser == null) {
            System.out.println("Authentication failed. Invalid username or password.");
        } else {
            System.out.println("Welcome, " + currentUser.getUsername() + "!");
        }
    }

    private void handleRegistration() {
        System.out.print("New username: ");
        String newUsername = scanner.nextLine();
        System.out.print("New password: ");
        String newPassword = scanner.nextLine();
        
        boolean registered = userManager.registerUser(newUsername, newPassword);
        if (registered) {
            System.out.println("Registration successful! You can now login.");
        } else {
            System.out.println("Username already exists. Please choose another one.");
        }
    }

    private void showBankingMenu() {
        Account account = currentUser.getAccount();
        
        System.out.println("\nWelcome to Simple Banking App - Logged in as: " + currentUser.getUsername());
        System.out.println("1. Deposit");
        System.out.println("2. Withdraw");
        System.out.println("3. List Transactions");
        System.out.println("4. Logout");
        System.out.println("5. Exit Application");
        System.out.print("Please choose an option: ");
        
        int choice = readIntInput();
        if (choice == -1) return;
        
        switch (choice) {
            case 1: handleDeposit(account); break;
            case 2: handleWithdrawal(account); break;
            case 3: account.listTransactions(); break;
            case 4: 
                currentUser = null;
                System.out.println("Logged out successfully.");
                break;
            case 5:
                currentUser = null;
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    private void handleDeposit(Account account) {
        System.out.print("Enter amount to deposit: ");
        double amount = readDoubleInput();
        if (amount > 0) {
            account.deposit(amount);
        } else {
            System.out.println("Deposit amount must be positive.");
        }
    }

    private void handleWithdrawal(Account account) {
        System.out.print("Enter amount to withdraw: ");
        double amount = readDoubleInput();
        if (amount > 0) {
            account.withdraw(amount);
        } else {
            System.out.println("Withdrawal amount must be positive.");
        }
    }

    private int readIntInput() {
        try {
            int input = Integer.parseInt(scanner.nextLine());
            return input;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1;
        }
    }

    private double readDoubleInput() {
        try {
            double input = Double.parseDouble(scanner.nextLine());
            return input;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
            return -1;
        }
    }
}