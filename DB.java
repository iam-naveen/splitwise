import java.sql.*;
import java.util.*;

/**
 * DB
 */
public class DB {
    static Connection conn;

    DB(Connection conn) {
        DB.conn = conn;
    }

    static int verify(String name, String password) throws SQLException {
        String query = String.format("SELECT * FROM user WHERE username = '%s' AND password = '%s'", name, password);
        ResultSet rs = DB.conn.createStatement().executeQuery(query);
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }

    static int createAccount(String name, String password) throws SQLException {
        String query = String.format("INSERT INTO user (username, password) VALUES ('%s', '%s')", name, password);
        return DB.conn.createStatement().executeUpdate(query);
    }

    static int createGroup(User user, String groupName, String memberNames) throws SQLException {
        PreparedStatement query = conn.prepareStatement("SELECT id FROM user WHERE username = ?");
        String[] members = memberNames.split(",");
        int[] memberIds = new int[members.length];
        for (int i = 0; i < members.length; i++) {
            query.setString(1, members[i].trim());
            ResultSet rs = query.executeQuery();
            if (!rs.next()) {
                System.out.println("User " + members[i].trim() + " does not exist");
                return -1;
            }
            memberIds[i] = rs.getInt("id");
        }
        String sql = String.format("INSERT INTO grp (name, created_user) VALUES ('%s', %d)", groupName, user.id);
        int row = DB.conn.createStatement().executeUpdate(sql);
        if (row <= 0)
            return row;
        sql = String.format("SELECT id FROM grp WHERE name = '%s' AND created_user = '%s'", groupName, user.id);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        rs.next();
        int groupId = rs.getInt("id");

        StringBuilder values = new StringBuilder();
        for (int i = 0; i < memberIds.length; i++) {
            values.append(String.format("(%d, %d)", groupId, memberIds[i]));
            if (i != memberIds.length - 1)
                values.append(", ");
        }
        sql = String.format("INSERT INTO user_grp_map (grp_id, user_id) VALUES %s", values.toString());

        return DB.conn.createStatement().executeUpdate(sql);
    }

    static int shareEqually(User user, String groupName, String expenseName, int amount) throws SQLException {
        String sql = String.format("SELECT id FROM grp WHERE name = '%s'", groupName);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        if (!rs.next()) {
            System.out.println("Group " + groupName + " does not exist");
            return -1;
        }
        int groupId = rs.getInt("id");
        sql = String.format("SELECT user_id FROM user_grp_map WHERE grp_id = %d", groupId);
        rs = DB.conn.createStatement().executeQuery(sql);

        ArrayList<Integer> members = new ArrayList<>();
        boolean userInGroup = false;
        while (rs.next()) {
            int id = rs.getInt("user_id");
            if (id == user.id) {
                userInGroup = true;
                continue;
            }
            members.add(id);
        }
        if (!userInGroup) {
            System.out.println("User " + user.name + " is not in the group " + groupName);
            return -1;
        }

        sql = String.format("INSERT INTO expense (name, grp_id, created_user, amount) VALUES ('%s', %d, %d, %d)",
                expenseName, groupId, user.id, amount);
        DB.conn.createStatement().executeUpdate(sql);
        sql = String.format(
                "SELECT id FROM expense WHERE grp_id = %d AND created_user = %d AND amount = %d ORDER BY created_at DESC LIMIT 1",
                groupId, user.id, amount);
        rs = DB.conn.createStatement().executeQuery(sql);
        rs.next();
        int expenseId = rs.getInt("id");

        int count = members.size() + 1; // also including the user
        int share = amount / count;
        StringBuilder values = new StringBuilder();
        int len = members.size();
        for (int i = 0; i < len; i++) {
            values.append(String.format("(%d, %d, %d)", expenseId, members.get(i), share));
            if (i != len - 1)
                values.append(", ");
        }
        sql = String.format("INSERT INTO share (expense_id, assigned_to, total_amount) VALUES %s", values.toString());
        return DB.conn.createStatement().executeUpdate(sql);
    }

    static int shareEquallyAmongSome(User user, String groupName, String expenseName, int amount, String members)
            throws SQLException {
        String sql = String.format("SELECT id FROM grp WHERE name = '%s'", groupName);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        if (!rs.next()) {
            System.out.println("Group " + groupName + " does not exist");
            return -1;
        }
        int groupId = rs.getInt("id");

        StringBuilder usernames = new StringBuilder("(");
        String[] memberNames = members.split(",");
        for (int i = 0; i < memberNames.length; i++) {
            usernames.append("'" + memberNames[i].trim() + "'");
            if (i != memberNames.length - 1)
                usernames.append(", ");
        }
        usernames.append(")");

        // getting all the valid members
        sql = String.format("SELECT * FROM user_grp_map M " +
                "JOIN user U ON user_id = id " +
                "WHERE grp_id = %d AND username IN %s", groupId, usernames);
        rs = DB.conn.createStatement().executeQuery(sql);
        HashMap<String, Integer> validMembers = new HashMap<>();
        while (rs.next()) {
            String name = rs.getString("username");
            int id = rs.getInt("user_id");
            validMembers.put(name, id);
        }

        if (validMembers.size() != memberNames.length) {
            System.out.println("Some of the members do not exist in the group " + groupName);
            return -1;
        }

        ArrayList<Integer> memberIds = new ArrayList<>();
        for (int i = 0; i < memberNames.length; i++) {
            if (validMembers.containsKey(memberNames[i].trim())) {
                int id = validMembers.get(memberNames[i]);
                if (id == user.id)
                    continue;
                memberIds.add(id);
            }
        }

        int count = memberIds.size() + 1;
        int share = amount / count;

        sql = String.format("INSERT INTO expense (name, grp_id, created_user, amount) VALUES ('%s', %d, %d, %d)",
                expenseName, groupId, user.id, amount);
        DB.conn.createStatement().executeUpdate(sql);
        sql = String.format(
                "SELECT id FROM expense WHERE grp_id = %d AND created_user = %d AND amount = %d ORDER BY created_at DESC LIMIT 1",
                groupId, user.id, amount);
        rs = DB.conn.createStatement().executeQuery(sql);
        rs.next();
        int expenseId = rs.getInt("id");

        StringBuilder values = new StringBuilder();
        for (int i = 0; i < count - 1; i++) {
            values.append(String.format("(%d, %d, %d)", expenseId, memberIds.get(i), share));
            if (i != count - 2)
                values.append(", ");
        }
        sql = String.format("INSERT INTO share (expense_id, assigned_to, total_amount) VALUES %s", values.toString());
        return DB.conn.createStatement().executeUpdate(sql);
    }

    static ResultSet getAllDues(User user) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', total_amount-paid_amount as '3' FROM share S " +
                        "JOIN expense E on S.expense_id=E.id " +
                        "JOIN user U ON E.created_user=U.id " +
                        "WHERE S.assigned_to = %d AND S.is_paid=false", user.id);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getDuesByGroup(User user, String groupName) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', total_amount-paid_amount as '3' FROM share S " +
                        "JOIN expense E on S.expense_id=E.id " +
                        "JOIN user U ON E.created_user=U.id " +
                        "JOIN grp G ON E.grp_id=G.id " +
                        "WHERE S.assigned_to = %d AND S.is_paid=false AND G.name='%s'", user.id, groupName);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getDuesByUser(User user, String userName) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', G.name as '2', total_amount-paid_amount as '3' FROM share S " +
                        "JOIN expense E on S.expense_id=E.id " +
                        "JOIN user U ON E.created_user=U.id " +
                        "JOIN grp G ON E.grp_id=G.id " +
                        "WHERE S.assigned_to = %d AND S.is_paid = false AND U.username='%s'", user.id, userName);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getAllPendingPayments(User user) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', S.total_amount-S.paid_amount as '3' FROM expense E " +
                        "JOIN share S ON E.id=S.expense_id " +
                        "JOIN user U ON S.assigned_to=U.id " +
                        "WHERE E.created_user = %d AND is_paid = false", user.id);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getPendingPaymentsByGroup(User user, String groupName) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', S.total_amount-S.paid_amount as '3' FROM expense E " +
                        "JOIN share S ON E.id=S.expense_id " +
                        "JOIN user U ON S.assigned_to=U.id " +
                        "JOIN grp G ON E.grp_id=G.id " +
                        "WHERE E.created_user = %d AND G.name='%s' AND is_paid = false", user.id, groupName);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getPendingPaymentsByUser(User user, String userName) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', S.total_amount-S.paid_amount as '3' FROM expense E " +
                        "JOIN share S ON E.id=S.expense_id " +
                        "JOIN user U ON S.assigned_to=U.id " +
                        "WHERE E.created_user = %d AND S.assigned_to='%s' AND is_paid = false", user.id, userName);
        return DB.conn.createStatement().executeQuery(sql);
    }
}