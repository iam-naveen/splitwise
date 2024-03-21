import java.sql.*;
import java.util.Scanner;

/**
 * Main
 */
public class Main {

    static Scanner get = new Scanner(System.in);
    static User user;

    void startApplication() throws SQLException {
        int choice = 0;
        while (choice != 3) {
            System.out.println("\n1. SignUp");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            choice = get.nextInt();
            get.nextLine();
            switch (choice) {
                case 1:
                    signUp();
                    break;
                case 2:
                    user = login();
                    if (user != null)
                        Interface.start(user);
                    break;
                case 3:
                    System.out.println("\nExiting...");
                    break;
                default:
                    System.out.println("\nInvalid choice");
            }
        }
    }

    void signUp() throws SQLException {
        System.out.print("Enter your name: ");
        String name = get.nextLine();
        System.out.print("Enter your password: ");
        String password = get.nextLine();
        int count = DB.createAccount(name, password);
        if (count > 0) {
            System.out.println("\nUser registered successfully");
        } else {
            System.out.println("\nError in registration");
        }
    }

    User login() throws SQLException {
        System.out.print("Enter your name: ");
        String name = get.nextLine();
        System.out.print("Enter your password: ");
        String password = get.nextLine();
        int id = DB.verify(name, password);
        if (id != -1) {
            System.out.println("\nLogin successful!!");
            return new User(id, name, password);
        } else {
            System.out.println("\nInvalid credentials...");
        }
        return null;
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/splitwise";
        String username = "root";
        String password = "";
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            new DB(conn);
            new Main().startApplication();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}