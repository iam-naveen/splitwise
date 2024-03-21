import java.sql.SQLException;
import java.util.Set;

public class Expense {
    int id;
    String name;
    int amount;
    User createdUser;

    public Expense(int id, String name, int amount, User createdUser) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.createdUser = createdUser;
    }

    static int shareEqually(String groupName, String expenseName, int amount) throws SQLException {
        int groupId = DB.getGroupId(groupName);
        if (groupId == -1) {
            System.out.println("\nGroup " + groupName + " does not exist");
            return -1;
        }
        Set<Integer> members = DB.getUsersInGroup(groupId);
        if (!members.contains(Main.user.id)) {
            System.out.println("\nYou are not Part of the group - " + groupName);
            return -1;
        }
        Expense expense = DB.createExpense(expenseName, amount, groupId);
        if (expense == null) {
            return -1;
        }
        return DB.createEqualShares(expense, amount, members);
    }

    static int shareEquallyAmongSome(String expenseName, int amount, String groupName, String members)
            throws SQLException {
        int groupId = DB.getGroupId(groupName);
        if (groupId == -1) {
            System.out.println("Group " + groupName + " does not exist");
            return -1;
        }
        if (!DB.checkIfUserInGroup(Main.user.id, groupId)) {
            System.out.println("\nYou are not part of the group");
            return -1;
        }
        members = "'" + members.trim().replace(",", "','") + "'";
        Set<Integer> validMembers = DB.getValidUsers(members, groupId);
        if (validMembers.size() != members.split(",").length) {
            System.out.println("\nSome users do not exist in the group");
            return -1;
        }
        Expense expense = DB.createExpense(expenseName, amount, groupId);
        return DB.createEqualShares(expense, amount, validMembers);
    }
}
