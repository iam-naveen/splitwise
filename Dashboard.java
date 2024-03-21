import java.sql.*;
import java.util.*;

/**
 * Interface
 */
public class Dashboard {
    static Scanner get = new Scanner(System.in);

    static void start() throws SQLException {
        int choice = 0;
        while (Main.user != null && choice != 6) {
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
                    System.out.print("Enter the group name: ");
                    String groupName = get.nextLine();
                    System.out.print("Enter the members: ");
                    String memberNames = get.nextLine().trim(); // the input is comma separated values
                    int status = Main.user.createGroupWithMembers(groupName, memberNames);
                    if (status > 0) {
                        System.out.println("\nGroup created successfully...");
                    } else {
                        System.out.println("Error in creating group");
                    }
                    break;
                case 2:
                    showExpenseOptions();
                    break;
                case 3:
                    showDueOptions();
                    break;
                case 4:
                    showPendingsOptions();
                    break;
                case 6:
                    Main.user = null; // logging out
                    break;
                default:
                    System.out.println("\nInvalid choice");
            }
        }
    }

    static void showExpenseOptions() throws SQLException {
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
                int status = Expense.shareEqually(groupName, expenseName, amount);
                if (status > 0)
                    System.out.println("\nExpense added successfully");
                else
                    System.out.println("Cannot add expense");
                break;
            case 2:
                System.out.print("Enter the member names: ");
                String memberNames = get.nextLine(); // the input is comma separated values
                status = Expense.shareEquallyAmongSome(expenseName, amount, groupName, memberNames);
                if (status > 0)
                    System.out.println("\nExpense added successfully");
                else
                    System.out.println("Cannot add expense");
                break;
            default:
                System.out.println("\nInvalid choice");
        }
    }

    static void showDueOptions() throws SQLException {
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
                res = Main.user.getMyDues();
                break;
            case 2:
                System.out.print("Enter the group name: ");
                String groupName = get.nextLine();
                res = Main.user.getMyDuesByGroupName(groupName);
                break;
            case 3:
                System.out.print("Enter the user name: ");
                String userName = get.nextLine();
                res = Main.user.getMyDuesByUsername(userName);
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

    static void showPendingsOptions() throws SQLException {
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
                res = Main.user.getAllPendings();
                break;
            case 2:
                System.out.print("Enter the group name: ");
                String groupName = get.nextLine();
                res = Main.user.getAllPendingsByGroup(groupName);
                break;
            case 3:
                System.out.print("Enter the user name: ");
                String userName = get.nextLine();
                res = Main.user.getAllPendingsByUsername(userName);
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
}