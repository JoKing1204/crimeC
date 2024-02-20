
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    static final String JDBC_DRIVER = "org.postgresql.Driver";
    static final String DB_URL = "jdbc:postgresql://localhost/crimev";
    static final String USER = "postgres";
    static final String PASS = "0928";
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

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            String sql = "SELECT fName, clearance_status FROM UserClearance WHERE fName = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String fName = rs.getString("fName");
                boolean clearance_status = rs.getBoolean("clearance_status");
                currentUser = new User(username, password, clearance_status); // Corrected line
                return true;
            } else {
                System.out.println("Incorrect username or password. Please try again.");
                return false;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error closing resources", e);
            }
        }
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
                System.out.println("4. Add new user");
                System.out.println("5. Search criminal by name");
                System.out.println("6. Search crime by ID");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();
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
                        System.out.println("Inserting criminal information");

                        break;
                    case 2:
                        System.out.println("Gathering all criminal information");
                        readCriminalData(conn);
                        break;
                    case 3:
                        System.out.print("Enter criminal ID to update: ");
                        long criminalIDToUpdate = scanner.nextLong();
                        scanner.nextLine(); // Consume newline
                        updateCriminalData(conn, criminalIDToUpdate);
                        System.out.println("Updating criminal information");
                        break;
                    case 4:
                        if (currentUser != null && currentUser.status) {
                            addUser(conn);
                        } else {
                            System.out.println("You don't have permission to add a new user.");
                        }

                        break;
                    case 5:
                        System.out.print("Enter the name of the criminal to search: ");
                        String searchName = scanner.nextLine();
                        searchCriminalByName(conn, searchName);
                        System.out.println("Searching for criminal ");
                        break;
                    case 6:
                        searchCrimeById(conn);
                        System.out.println("Searching for crime ");
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

    private static void searchCrimeById(Connection conn) {
        try {
            System.out.print("Enter crime ID to search: ");
            int crimeId = scanner.nextInt();
            scanner.nextLine();

            String searchSQL = "SELECT * FROM CrimeDatabase WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {
                pstmt.setInt(1, crimeId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String crime = rs.getString("crime");
                        System.out.println("Crime ID: " + crimeId);
                        System.out.println("Crime Description: " + crime);
                    } else {
                        System.out.println("No crime found with ID: " + crimeId);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching crime by ID", e);
        }
    }

    private static void searchCriminalByName(Connection conn, String searchName) throws SQLException {
        String searchSQL = "SELECT * FROM criminals WHERE fName = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {
            pstmt.setString(1, searchName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
                } else {
                    System.out.println("No criminal found with the specified name.");
                }
            }
        } catch (SQLException se) {
            throw new SQLException("Error searching criminal by name", se);
        }
    }


    private static void readUsersData(Connection conn) throws SQLException {
        String selectUsersSQL = "SELECT fName, clearance_status FROM UserClearance";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectUsersSQL)) {
            System.out.println("Users Data:");
            System.out.println("Username\tStatus");
            while (rs.next()) {
                String username = rs.getString("fName");
                boolean status = rs.getBoolean("clearance_status");
                System.out.println(username + "\t\t" + (status ? "Active" : "Inactive"));
            }
        } catch (SQLException se) {
            throw new SQLException("Error reading users data", se);
        }
    }
    private static void addUser(Connection conn) throws SQLException {
        System.out.println("Enter new username:");
        String newUsername = scanner.nextLine();
        System.out.println("Enter new password:");
        String newPassword = scanner.nextLine();
        System.out.println("Enter new status (true/false):");
        boolean newStatus = scanner.nextBoolean();
        scanner.nextLine(); // Consume newline

        // Insert new user into the database
        String insertUserSQL = "INSERT INTO UserClearance (fName, password, clearance_status) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertUserSQL)) {
            pstmt.setString(1, newUsername);
            pstmt.setString(2, newPassword);
            pstmt.setBoolean(3, newStatus);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("New user added successfully.");
            } else {
                System.out.println("Failed to add new user.");
            }
        } catch (SQLException se) {
            throw new SQLException("Error adding new user", se);
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

    private static void updateCriminalData(Connection conn, long criminalID) throws SQLException {
        try {
            Scanner scanner = new Scanner(System.in);
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
            scanner.nextLine(); // Consume newline

            String updateSQL = "UPDATE criminals SET fName = ?, age = ?, status = ?, address = ?, date_admission = ?, " +
                    "crimeID = ?, return_count = ? WHERE criminalID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, fName);
                pstmt.setInt(2, age);
                pstmt.setString(3, status);
                pstmt.setString(4, address);
                pstmt.setDate(5, Date.valueOf(date_admission));
                pstmt.setLong(6, crimeID);
                pstmt.setInt(7, return_count);
                pstmt.setLong(8, criminalID);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    logger.info("Criminal data updated successfully.");
                } else {
                    logger.warning("No criminal found with the provided ID.");
                }
            }
        } catch (SQLException se) {
            logger.log(Level.SEVERE, "Error updating criminal data", se);
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



