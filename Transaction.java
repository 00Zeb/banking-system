/**
 * Represents a single transaction with a type and an amount.
 */
public class Transaction {
    private String type;
    private double amount;

    /**
     * Constructor for Transaction.
     * @param type The type of transaction (e.g., "Deposit", "Withdrawal").
     * @param amount The amount of the transaction.
     */
    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    /**
     * Returns a string representation of the transaction.
     * @return Formatted string with transaction type and amount.
     */
    @Override
    public String toString() {
        return "Type: " + type + ", Amount: $" + String.format("%.2f", amount);
    }
}
