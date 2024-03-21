import java.sql.*;

/**
 * User
 */
public class User {
    int id;
    String name;
    String password;

    public User(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    ResultSet getMyDues() throws SQLException {
        return DB.getAllDues(this.id);
    }

    ResultSet getMyDuesByGroupName(String groupName) throws SQLException {
        return DB.getDuesByGroup(this.id, groupName);
    }

    ResultSet getMyDuesByUsername(String userName) throws SQLException {
        return DB.getDuesByUser(this.id, userName);
    }

    ResultSet getAllPendings() throws SQLException {
        return DB.getAllPendingPayments(this.id);
    }

    ResultSet getAllPendingsByGroup(String groupName) throws SQLException {
        return DB.getPendingPaymentsByGroup(this.id, groupName);
    }

    ResultSet getAllPendingsByUsername(String userName) throws SQLException {
        return DB.getPendingPaymentsByUser(this.id, userName);
    }

    int createGroupWithMembers(String groupName, String members) throws SQLException {
        String[] memberNames = members.split(",");
        int[] memberIds = new int[memberNames.length];
        for (int i = 0; i < memberNames.length; i++) {
            int id = DB.getUserId(memberNames[i]);
            if (id < 0) {
                System.out.println("\n" + memberNames[i] + " is not a valid username");
                return -1;
            }
            memberIds[i] = id;
        }
        int groupId = DB.createGroup(this.id, groupName);
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < memberIds.length; i++) {
            values.append(String.format("(%d, %d)", groupId, memberIds[i]));
            if (i != memberIds.length - 1)
                values.append(", ");
        }
        return DB.insertIntoGroup(values.toString());
    }
}
