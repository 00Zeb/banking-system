import java.util.Scanner;

/**
 * Main class for the banking application.
 * Handles user interaction and the main application loop.
 */
public class BankingApp {

    public static void main(String[] args) {
        // Scanner for user input
        Scanner scanner = new Scanner(System.in);
        // Account object to manage banking operations
        Account account = new Account();
        // Variable to control the main loop
        boolean exit = false;

        // Main application loop
        while (!exit) {
            // Display the menu
            System.out.println("\nWelcome to Simple Banking App");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. List Transactions");
            System.out.println("4. Exit");
            System.out.print("Please choose an option: ");

            // Read user's choice
            int choice = -1;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // Consume the invalid input
                continue;
            }


            // Process the user's choice
            switch (choice) {
                case 1:
                    // Deposit
                    System.out.print("Enter amount to deposit: ");
                    if (scanner.hasNextDouble()) {
                        double depositAmount = scanner.nextDouble();
                        if (depositAmount <= 0) {
                            System.out.println("Deposit amount must be positive.");
                        } else {
                            account.deposit(depositAmount);
                        }
                    } else {
                        System.out.println("Invalid amount. Please enter a number.");
                        scanner.next(); // Consume invalid input
                    }
                    break;
                case 2:
                    // Withdrawal
                    System.out.print("Enter amount to withdraw: ");
                     if (scanner.hasNextDouble()) {
                        double withdrawalAmount = scanner.nextDouble();
                        if (withdrawalAmount <= 0) {
                            System.out.println("Withdrawal amount must be positive.");
                        } else {
                            account.withdraw(withdrawalAmount);
                        }
                    } else {
                        System.out.println("Invalid amount. Please enter a number.");
                        scanner.next(); // Consume invalid input
                    }
                    break;
                case 3:
                    // List Transactions
                    account.listTransactions();
                    break;
                case 4:
                    // Exit
                    exit = true;
                    System.out.println("Thank you for using Simple Banking App!");
                    break;
                default:
                    // Invalid option
                    System.out.println("Invalid option. Please try again.");
            }
        }

        // Close the scanner
        scanner.close();
    }
}
