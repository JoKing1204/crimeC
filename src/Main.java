
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    static final String JDBC_DRIVER = "org.postgresql.Driver";
    static final String DB_URL = "jdbc:postgresql://localhost/crimev";
    static final String USER = "postgres";
    static final String PASS = "0212";
    static class User {
        String username;
        String password;
        boolean status;

        User(String username, String password, boolean status) {
            this.username = username;
            this.password = password;
            this.status = status;
        }
    }

    // Hardcoded list of user accounts (for demonstration)
    private static User[] users = {
            new User("user1", "password1", true),
            new User("user2", "password2", true),
            new User("user3", "password3", true)
    };

    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("Welcome to the Login System!");

        boolean isValid = false;
        while (!isValid) {
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Choose an option(1/2): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    isValid = login();
                    if (isValid) {
                        System.out.println("Login successful. Welcome, " + currentUser.username + "!");
                        startCRUD();
                    }
                    break;
                case 2:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static boolean login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        for (User user : users) {
            if (user.username.equals(username) && user.password.equals(password)) {
                if (user.status) {
                    currentUser = user;
                    return true;
                } else {
                    System.out.println("Your account is not active. Please contact an administrator.");
                    return false;
                }
            }
        }
        System.out.println("Incorrect username or password. Please try again.");
        return false;
    }
    private static void startCRUD() {
        Connection conn = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);

            // Create tables if not exists
            createTables(conn);

            Scanner scanner = new Scanner(System.in);
            int choice;
            do {
                System.out.println("1. Insert criminal data");
                System.out.println("2. Read criminal data");
                System.out.println("3. Update criminal data");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        scanner.nextLine(); // Consume newline
                        System.out.print("Enter criminal's first name: ");
                        String fName = scanner.nextLine();
                        System.out.print("Enter criminal's age: ");
                        int age = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        System.out.print("Enter criminal's status: ");
                        String status = scanner.nextLine();
                        System.out.print("Enter criminal's address: ");
                        String address = scanner.nextLine();
                        System.out.print("Enter date of admission (YYYY-MM-DD): ");
                        String date_admission = scanner.nextLine();
                        System.out.print("Enter crime ID: ");
                        long crimeID = scanner.nextLong();
                        System.out.print("Enter return count: ");
                        int return_count = scanner.nextInt();
                        insertCriminalData(conn, fName, age, status, address, date_admission, crimeID, return_count);
                        break;
                    case 2:
                        readCriminalData(conn);
                        break;
                    case 3:
                        System.out.print("Enter criminal ID to update: ");
                        long criminalIDToUpdate = scanner.nextLong();
                        scanner.nextLine(); // Consume newline
                        System.out.print("Enter new status: ");
                        String newStatus = scanner.nextLine();
                        updateCriminalData(conn, criminalIDToUpdate, newStatus);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                }
            } while (choice != 0);

            // Commit transaction
            conn.commit();

            conn.close();
        } catch (SQLException se) {
            logger.log(Level.SEVERE, "SQL Error", se);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException se2) {
                logger.log(Level.SEVERE, "Rollback Error", se2);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected Error", e);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String createCrimeDatabaseTableSQL = "CREATE TABLE IF NOT EXISTS CrimeDatabase (" +
                "id BIGSERIAL NOT NULL PRIMARY KEY," +
                "crime VARCHAR(900)" +
                ")";
        String createCriminalsTableSQL = "CREATE TABLE IF NOT EXISTS criminals (" +
                "criminalID BIGSERIAL NOT NULL PRIMARY KEY," +
                "fName VARCHAR(30) NOT NULL," +
                "age INT NOT NULL," +
                "status VARCHAR(30) NOT NULL," +
                "address VARCHAR(150)," +
                "date_admission DATE NOT NULL," +
                "crimeID BIGINT REFERENCES CrimeDatabase(id)," +
                "return_count INT" +
                ")";
        try (Statement statement = conn.createStatement()) {
            statement.execute(createCrimeDatabaseTableSQL);
            statement.execute(createCriminalsTableSQL);
        } catch (SQLException se) {
            throw new SQLException("Error creating tables", se);
        }
    }

    private static void insertCriminalData(Connection conn, String fName, int age, String status, String address,
                                           String date_admission, long crimeID, int return_count) throws SQLException {
        String insertSQL = "INSERT INTO criminals (fName, age, status, address, date_admission, crimeID, return_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, fName);
            pstmt.setInt(2, age);
            pstmt.setString(3, status);
            pstmt.setString(4, address);
            pstmt.setDate(5, Date.valueOf(date_admission));
            pstmt.setLong(6, crimeID);
            pstmt.setInt(7, return_count);
            pstmt.executeUpdate();
            logger.info("Criminal data inserted successfully.");
        } catch (SQLException se) {
            throw new SQLException("Error inserting criminal data", se);
        }
    }

    private static void readCriminalData(Connection conn) throws SQLException {
        String selectSQL = "SELECT * FROM criminals";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                long criminalID = rs.getLong("criminalID");
                String fName = rs.getString("fName");
                int age = rs.getInt("age");
                String status = rs.getString("status");
                String address = rs.getString("address");
                Date date_admission = rs.getDate("date_admission");
                long crimeID = rs.getLong("crimeID");
                int return_count = rs.getInt("return_count");
                logger.info("Criminal ID: " + criminalID + ", First Name: " + fName +
                        ", Age: " + age + ", Status: " + status +
                        ", Address: " + address + ", Date Admission: " + date_admission +
                        ", Crime ID: " + crimeID + ", Return Count: " + return_count);
            }
        } catch (SQLException se) {
            throw new SQLException("Error reading criminal data", se);
        }
    }

    private static void updateCriminalData(Connection conn, long criminalID, String newStatus) throws SQLException {
        String updateSQL = "UPDATE criminals SET status = ? WHERE criminalID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, newStatus);
            pstmt.setLong(2, criminalID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Criminal data updated successfully.");
            } else {
                logger.warning("No criminal found with the provided ID.");
            }
        } catch (SQLException se) {
            throw new SQLException("Error updating criminal data", se);
        }
    }

//    private static void deleteCriminalData(Connection conn, long criminalID) throws SQLException {
//        String deleteSQL = "DELETE FROM criminals WHERE criminalID = ?";
//        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
//            pstmt.setLong(1, criminalID);
//            int affectedRows = pstmt.executeUpdate();
//            if (affectedRows > 0) {
//                logger.info("Criminal data deleted successfully.");
//            } else {
//                logger.warning("No criminal found with the provided ID.");
//            }
//        } catch (SQLException se) {
//            throw new SQLException("Error deleting criminal data", se);
//        }
//    }

    }


