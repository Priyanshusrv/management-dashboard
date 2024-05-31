import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BankAccount {
    private int accountId;
    private int userId;
    private String accountNumber;
    private String accountHolderName;
    private double balance;

    // Constructor, getters, and setters

    public BankAccount(int userId, String accountNumber, String accountHolderName, double initialBalance) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = initialBalance;
    }

    public boolean createAccount() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Accounts (user_id, account_number, account_holder_name, balance) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, accountNumber);
            stmt.setString(3, accountHolderName);
            stmt.setDouble(4, balance);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static BankAccount getAccountByNumber(String accountNumber) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Accounts WHERE account_number = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new BankAccount(
                    rs.getInt("user_id"),
                    rs.getString("account_number"),
                    rs.getString("account_holder_name"),
                    rs.getDouble("balance")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deposit(double amount) {
        if (amount <= 0) return false;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            String updateBalanceQuery = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement updateBalanceStmt = connection.prepareStatement(updateBalanceQuery);
            updateBalanceStmt.setDouble(1, amount);
            updateBalanceStmt.setString(2, accountNumber);
            updateBalanceStmt.executeUpdate();

            String insertTransactionQuery = "INSERT INTO Transactions (account_number, transaction_type, amount) VALUES (?, ?, ?)";
            PreparedStatement insertTransactionStmt = connection.prepareStatement(insertTransactionQuery);
            insertTransactionStmt.setString(1, accountNumber);
            insertTransactionStmt.setString(2, "DEPOSIT");
            insertTransactionStmt.setDouble(3, amount);
            insertTransactionStmt.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            String updateBalanceQuery = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
            PreparedStatement updateBalanceStmt = connection.prepareStatement(updateBalanceQuery);
            updateBalanceStmt.setDouble(1, amount);
            updateBalanceStmt.setString(2, accountNumber);
            updateBalanceStmt.executeUpdate();

            String insertTransactionQuery = "INSERT INTO Transactions (account_number, transaction_type, amount) VALUES (?, ?, ?)";
            PreparedStatement insertTransactionStmt = connection.prepareStatement(insertTransactionQuery);
            insertTransactionStmt.setString(1, accountNumber);
            insertTransactionStmt.setString(2, "WITHDRAW");
            insertTransactionStmt.setDouble(3, amount);
            insertTransactionStmt.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getBalance() {
        return balance;
    }

    public void printTransactionHistory() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Transactions WHERE account_number = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Transaction History for Account: " + accountNumber);
            while (rs.next()) {
                System.out.println(
                    rs.getTimestamp("transaction_date") + " - " +
                    rs.getString("transaction_type") + " - $" +
                    rs.getDouble("amount")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
