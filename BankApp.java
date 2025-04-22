import java.sql.*;
import java.util.Scanner;

public class BankApp {
    static final String URL = "jdbc:mysql://localhost:3306/bankdb";
    static final String USER = "root";
    static final String PASS = "root123";

    static Scanner sc = new Scanner(System.in);
    static int userId = -1;

    public static void main(String[] args) {
        try (
                //Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bankdb", "root", "root123")) {
            while (true) {
                System.out.println("\n Welcome to Bank");
                System.out.println("1. Register\n2. Login\n3. Exit");
                System.out.print("Choose: ");
                int choice = sc.nextInt();
                sc.nextLine();

                if (choice == 1) register(conn);
                else if (choice == 2) {
                    if (login(conn)) userMenu(conn);
                } else break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void register(Connection conn) throws SQLException {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);

        try {
            ps.executeUpdate();
            System.out.println("Registered successfully.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username already exists.");
        }
    }

    static boolean login(Connection conn) throws SQLException {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            userId = rs.getInt("id");
            System.out.println("Login successful!");
            return true;
        } else {
            System.out.println("Invalid credentials.");
            return false;
        }
    }

    static void userMenu(Connection conn) throws SQLException {
        while (true) {
            System.out.println("\n1. Check Balance\n2. Deposit\n3. Withdraw\n4. View Transactions\n5. Logout");
            System.out.print("Choose: ");
            int choice = sc.nextInt();

            if (choice == 1) checkBalance(conn);
            else if (choice == 2) deposit(conn);
            else if (choice == 3) withdraw(conn);
            else if (choice == 4) viewTransactions(conn);
            else break;
        }
    }

    static void checkBalance(Connection conn) throws SQLException {
        String sql = "SELECT balance FROM users WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            System.out.println("Your balance: ₹" + rs.getDouble("balance"));
        }
    }

    static void deposit(Connection conn) throws SQLException {
        System.out.print("Enter amount to deposit: ₹");
        double amount = sc.nextDouble();

        String update = "UPDATE users SET balance = balance + ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(update);
        ps.setDouble(1, amount);
        ps.setInt(2, userId);
        ps.executeUpdate();

        logTransaction(conn, "Deposit", amount);
        System.out.println("Deposited ₹" + amount);
    }

    static void withdraw(Connection conn) throws SQLException {
        System.out.print("Enter amount to withdraw: ₹");
        double amount = sc.nextDouble();

        String check = "SELECT balance FROM users WHERE id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(check);
        checkStmt.setInt(1, userId);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next() && rs.getDouble("balance") >= amount) {
            String update = "UPDATE users SET balance = balance - ? WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(update);
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();

            logTransaction(conn, "Withdraw", amount);
            System.out.println("Withdrawn ₹" + amount);
        } else {
            System.out.println("Insufficient balance.");
        }
    }

    static void viewTransactions(Connection conn) throws SQLException {
        String sql = "SELECT type, amount, timestamp FROM transactions WHERE user_id = ? ORDER BY timestamp DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        System.out.println("Transaction History:");
        while (rs.next()) {
            System.out.printf("%s: ₹%.2f on %s\n",
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("timestamp"));
        }
    }

    static void logTransaction(Connection conn, String type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, type, amount) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.executeUpdate();
    }
}
