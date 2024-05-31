import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Loan {
    private int loanId;
    private int userId;
    private double loanAmount;
    private double interestRate;
    private String loanStatus;

    // Constructor, getters, and setters

    public Loan(int userId, double loanAmount, double interestRate) {
        this.userId = userId;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.loanStatus = "Pending";
    }

    public boolean applyLoan() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO Loans (user_id, loan_amount, interest_rate, loan_status) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setDouble(2, loanAmount);
            stmt.setDouble(3, interestRate);
            stmt.setString(4, loanStatus);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void getUserLoans(int userId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Loans WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(
