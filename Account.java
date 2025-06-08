import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account, managing balance and transactions.
 */
public class Account {
    private double balance;
    private List<Transaction> transactions;

    /**
     * Constructor for Account.
     * Initializes balance to 0 and creates a new list for transactions.
     */
    public Account() {
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    /**
     * Deposits a specified amount into the account.
     * @param amount The amount to deposit. Must be positive.
     */
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactions.add(new Transaction("Deposit", amount));
            System.out.println("Successfully deposited $" + amount);
            System.out.println("Current Balance: $" + getBalance());
        } else {
            System.out.println("Deposit amount must be positive.");
        }
    }

    /**
     * Withdraws a specified amount from the account.
     * @param amount The amount to withdraw. Must be positive and not exceed the balance.
     */
    public void withdraw(double amount) {
        if (amount > 0) {
            if (balance >= amount) {
                balance -= amount;
                transactions.add(new Transaction("Withdrawal", amount));
                System.out.println("Successfully withdrew $" + amount);
                System.out.println("Current Balance: $" + getBalance());
            } else {
                System.out.println("Insufficient funds. Withdrawal failed.");
            }
        } else {
            System.out.println("Withdrawal amount must be positive.");
        }
    }

    /**
     * Lists all transactions for the account.
     */
    public void listTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            System.out.println("\n--- Transaction History ---");
            for (Transaction t : transactions) {
                System.out.println(t);
            }
            System.out.println("-------------------------");
        }
        System.out.println("Current Balance: $" + getBalance());
    }
    
    /**
     * Returns the current account balance formatted to two decimal places.
     * @return The formatted balance string.
     */
    public String getBalance() {
        return String.format("%.2f", balance);
    }
}
