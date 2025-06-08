package com.example.banking;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for the banking application.
 * Handles the main application flow including user authentication and banking operations.
 */
public class BankingApp {

    private static final UserService userService = new UserService();
    // Maps a username to their specific Account object
    private static final Map<String, Account> userAccounts = new HashMap<>();
    private static User currentUser = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            // If no user is logged in, show the auth menu
            if (currentUser == null) {
                showAuthMenu();
            } else {
                // If a user is logged in, show the banking menu
                showBankingMenu();
            }
        }
    }

    /**
     * Displays and handles the initial authentication menu (Login/Register/Exit).
     */
    private static void showAuthMenu() {
        System.out.println("\n--- Welcome to Simple Banking ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Please choose an option: ");

        int choice = getIntInput();

        switch (choice) {
            case 1:
                handleLogin();
                break;
            case 2:
                handleRegistration();
                break;
            case 3:
                System.out.println("Thank you for using our bank. Goodbye!");
                scanner.close();
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    /**
     * Displays and handles the main banking menu after a user has logged in.
     */
    private static void showBankingMenu() {
        System.out.println("\n--- Banking Menu ---");
        System.out.println("Welcome, " + currentUser.getUsername() + "!");
        System.out.println("1. Deposit");
        System.out.println("2. Withdraw");
        System.out.println("3. List Transactions");
        System.out.println("4. Logout");
        System.out.print("Please choose an option: ");

        // Get the account for the currently logged-in user
        Account currentAccount = userAccounts.get(currentUser.getUsername());

        int choice = getIntInput();

        switch (choice) {
            case 1:
                System.out.print("Enter amount to deposit: ");
                double depositAmount = getDoubleInput();
                currentAccount.deposit(depositAmount);
                break;
            case 2:
                System.out.print("Enter amount to withdraw: ");
                double withdrawalAmount = getDoubleInput();
                currentAccount.withdraw(withdrawalAmount);
                break;
            case 3:
                currentAccount.listTransactions();
                break;
            case 4:
                // Logout by setting the current user to null
                currentUser = null;
                System.out.println("You have been logged out.");
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }

    /**
     * Handles the user registration process.
     */
    private static void handleRegistration() {
        System.out.print("Enter new username: ");
        String username = scanner.next();
        System.out.print("Enter new password: ");
        String password = scanner.next();

        if (userService.registerUser(username, password)) {
            // Create a new bank account for the new user
            userAccounts.put(username, new Account());
        }
    }

    /**
     * Handles the user login process.
     */
    private static void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();
        // Attempt to log in and update the currentUser if successful
        currentUser = userService.loginUser(username, password);
    }

    /**
     * Helper method to reliably get an integer from the scanner.
     */
    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next(); // discard non-int input
        }
        return scanner.nextInt();
    }

    /**
     * Helper method to reliably get a double from the scanner.
     */
    private static double getDoubleInput() {
        while (!scanner.hasNextDouble()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next(); // discard non-double input
        }
        return scanner.nextDouble();
    }
}
