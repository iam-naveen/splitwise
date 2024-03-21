import java.sql.*;
import java.util.Scanner;

/**
 * Interface
 */
public class Interface {
    static Scanner get = new Scanner(System.in);
    static User user;

    static void start(User user) throws SQLException {
        Interface.user = user;

        int choice = 0;
        while (user != null && choice != 6) {
            System.out.println("\n1. Create a Group");
            System.out.println("2. Add Expense in a Group");
            System.out.println("3. List all My Dues");
            System.out.println("4. List all Payments to be Received");
            System.out.println("6. Logout");

            System.out.print("Enter your choice: ");
            choice = get.nextInt();
            get.nextLine();
            switch (choice) {
                case 1:
                    createGroup();
                    break;
                case 2:
                    addExpense();
                    break;
                case 3:
                    listDuePayments();
                    break;
                case 4:
                    listPendingPayments();
                    break;
                case 6:
                    logout();
                    break;
                default:
                    System.out.println("\nInvalid choice");
            }
        }
    }

    static void createGroup() throws SQLException {
        System.out.print("Enter the group name: ");
        String groupName = get.nextLine();
        System.out.print("Enter the members: ");
        String memberNames = get.nextLine().trim(); // the input is comma separated values
        int status = DB.createGroup(user, groupName, memberNames);
        if (status > 0) {
            System.out.println("\nGroup created successfully...");
        } else {
            System.out.println("\nError in creating group");
        }
    }

    static void addExpense() throws SQLException {
        System.out.print("Enter the group name: ");
        String groupName = get.nextLine();
        System.out.print("Enter the Expense Name: ");
        String expenseName = get.nextLine();
        System.out.print("Enter the Expense amount: ");
        int amount = get.nextInt();
        get.nextLine();

        int choice = 0;
        System.out.println("\t1. Share Expense equally among all members");
        System.out.println("\t2. Share Expense equally among some members");
        System.out.print("Enter your choice: ");
        choice = get.nextInt();
        get.nextLine();
        switch (choice) {
            case 1:
                int status = DB.shareEqually(user, groupName, expenseName, amount);
                if (status > 0)
                    System.out.println("\nExpense added successfully");
                else
                    System.out.println("\nError in adding expense");
                break;
            case 2:
                System.out.print("Enter the member names: ");
                String memberNames = get.nextLine(); // the input is comma separated values
                status = DB.shareEquallyAmongSome(user, groupName, expenseName, amount, memberNames);
                if (status > 0)
                    System.out.println("\nExpense added successfully");
                else
                    System.out.println("\nError in adding expense");
                break;
            default:
                System.out.println("\nInvalid choice");
        }
    }

    static void listDuePayments() throws SQLException {
        System.out.println("\n\tChoose an option");
        System.out.println("\t1. Show All");
        System.out.println("\t2. Filter by Goup");
        System.out.println("\t3. Filter by User");
        System.out.print("Enter your choice: ");

        int choice = get.nextInt();
        get.nextLine();
        ResultSet res = null;
        switch (choice) {
            case 1:
                res = DB.getAllDues(user);
                break;
            case 2:
                System.out.print("Enter the group name: ");
                String groupName = get.nextLine();
                res = DB.getDuesByGroup(user, groupName);
                break;
            case 3:
                System.out.print("Enter the user name: ");
                String userName = get.nextLine();
                res = DB.getDuesByUser(user, userName);
                break;
            default:
                System.out.println("\nInvalid choice");
        }
        if (res == null || !res.next()) {
            System.out.println("\nNo due payments");
            return;
        }
        int total = 0;
        StringBuilder pp = new StringBuilder("Expense\t|\tTo\t|\tAmount\n");
        pp.append("=========================================\n");
        do {
            pp.append(res.getString("1") + "\t|\t");
            pp.append(res.getString("2") + "\t|\t");
            pp.append("Rs." + res.getInt("3") + "\n");
            total += res.getInt("3");
        } while (res.next());
        System.out.println("\nDue Payments");
        System.out.println("=========================================");
        System.out.print(pp.toString());
        System.out.println("=========================================");
        System.out.println("Total:\tRs." + total);
        System.out.println("=========================================");
    }

    static void listPendingPayments() throws SQLException {
        System.out.println("\n\tChoose an option");
        System.out.println("\t1. Show All");
        System.out.println("\t2. Filter by Goup");
        System.out.println("\t3. Filter by User");
        System.out.print("Enter your choice: ");

        int choice = get.nextInt();
        get.nextLine();
        ResultSet res = null;
        switch (choice) {
            case 1:
                res = DB.getAllPendingPayments(user);
                break;
            case 2:
                System.out.print("Enter the group name: ");
                String groupName = get.nextLine();
                res = DB.getPendingPaymentsByGroup(user, groupName);
                break;
            case 3:
                System.out.print("Enter the user name: ");
                String userName = get.nextLine();
                res = DB.getPendingPaymentsByUser(user, userName);
                break;
            default:
                System.out.println("\nInvalid choice");
        }
        if (res == null || !res.next()) {
            System.out.println("\nNo pending payments...");
            return;
        }
        int total = 0;
        StringBuilder pp = new StringBuilder("Expense\t|\tFrom\t|\tAmount\n");
        pp.append("=========================================\n");
        do {
            pp.append(res.getString("1") + "\t|\t");
            pp.append(res.getString("2") + "\t|\t");
            pp.append("Rs." + res.getInt("3") + "\n");
            total += res.getInt("3");
        } while (res.next());
        System.out.println("\n=========================================");
        System.out.println("Pending Payments to be Received");
        System.out.println("=========================================");
        System.out.print(pp.toString());
        System.out.println("=========================================");
        System.out.println("Total:\tRs." + total);
        System.out.println("=========================================");
    }

    static void logout() {
        user = null;
    }
}