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

    static int createGroup(int userId, String groupName) throws SQLException {
        String sql = String.format("INSERT INTO grp (name, created_user) VALUES ('%s', %d)", groupName, userId);
        DB.conn.createStatement().executeUpdate(sql);
        sql = String.format("SELECT id FROM grp WHERE name = '%s' AND created_user = '%s'", groupName, userId);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        rs.next();
        return rs.getInt("id");
    }

    static int getUserId(String username) throws SQLException {
        String sql = String.format("SELECT id FROM user WHERE username = '%s'", username);
        ResultSet res = DB.conn.createStatement().executeQuery(sql);
        if (!res.next())
            return -1;
        return res.getInt("id");
    }

    static int insertIntoGroup(String values) throws SQLException {
        String sql = String.format("INSERT INTO user_grp_map (grp_id, user_id) VALUES %s", values.toString());
        return DB.conn.createStatement().executeUpdate(sql);
    }

    static Integer getGroupId(String groupName, User user) throws SQLException {
        String sql = String.format("SELECT id FROM grp WHERE name = '%s' AND created_user = %d", groupName, user.id);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        if (rs.next())
            return rs.getInt("id");
        return null;
    }

    static Integer getGroupId(String groupName) throws SQLException {
        String sql = String.format("SELECT id FROM grp WHERE name = '%s'", groupName);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        if (rs.next())
            return rs.getInt("id");
        return -1;
    }

    static Set<Integer> getUsersInGroup(Integer groupId) throws SQLException {
        String sql = String.format("SELECT user_id FROM user_grp_map WHERE grp_id = %d", groupId);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        Set<Integer> users = new HashSet<>();
        while (rs.next()) {
            users.add(rs.getInt("user_id"));
        }
        return users;
    }

    static Expense createExpense(String name, int amount, int groupId) throws SQLException {
        String sql = String.format("INSERT INTO expense (name, grp_id, created_user, amount) VALUES ('%s', %d, %d, %d)",
                name, groupId, Main.user.id, amount);
        DB.conn.createStatement().executeUpdate(sql);
        sql = String.format(
                "SELECT id FROM expense WHERE grp_id = %d AND created_user = %d AND amount = %d ORDER BY created_at DESC LIMIT 1",
                groupId, Main.user.id, amount);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        if (rs.next())
            return new Expense(rs.getInt("id"), name, amount, Main.user);
        return null;
    }

    static int createEqualShares(Expense expense, int totalAmount, Set<Integer> users) throws SQLException {
        StringBuilder values = new StringBuilder();
        int len = users.size();
        users.stream().forEach((userId) -> {
            if (userId == Main.user.id)
                return;
            values.append(String.format("(%d, %d, %d),", expense.id, userId, totalAmount / len));
        });
        values.deleteCharAt(values.length() - 1);
        String sql = String.format("INSERT INTO share (expense_id, assigned_to, total_amount) VALUES %s",
                values.toString());
        return DB.conn.createStatement().executeUpdate(sql);
    }

    static Set<Integer> getValidUsers(String users, int groupId) throws SQLException {
        Set<Integer> validUsers = new HashSet<>();
        String sql = String.format("""
                        SELECT id FROM user U
                        JOIN user_grp_map M ON user_id = id
                        WHERE grp_id = %d AND username IN (%s)
                """, groupId, users);
        ResultSet rs = DB.conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            validUsers.add(rs.getInt("id"));
        }
        return validUsers;
    }

    static boolean checkIfUserInGroup(int userId, int groupId) throws SQLException {
        ResultSet rs = DB.conn.createStatement()
                .executeQuery("SELECT * FROM user_grp_map WHERE grp_id = " + groupId + " AND user_id = " + userId);
        return rs.next();
    }

    static ResultSet getAllDues(int userId) throws SQLException {
        String sql = String.format("""
                        SELECT E.name as '1', U.username as '2', total_amount-paid_amount as '3'
                        FROM share S
                        JOIN expense E on S.expense_id=E.id
                        JOIN user U ON E.created_user=U.id
                        WHERE S.assigned_to = %d AND S.is_paid=false
                """, userId);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getDuesByGroup(int userId, String groupName) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', total_amount-paid_amount as '3' FROM share S " +
                        "JOIN expense E on S.expense_id=E.id " +
                        "JOIN user U ON E.created_user=U.id " +
                        "JOIN grp G ON E.grp_id=G.id " +
                        "WHERE S.assigned_to = %d AND S.is_paid=false AND G.name='%s'", userId, groupName);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getDuesByUser(int userId, String userName) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', G.name as '2', total_amount-paid_amount as '3' FROM share S " +
                        "JOIN expense E on S.expense_id=E.id " +
                        "JOIN user U ON E.created_user=U.id " +
                        "JOIN grp G ON E.grp_id=G.id " +
                        "WHERE S.assigned_to = %d AND S.is_paid = false AND U.username='%s'", userId, userName);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getAllPendingPayments(int userId) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', S.total_amount-S.paid_amount as '3' FROM expense E " +
                        "JOIN share S ON E.id=S.expense_id " +
                        "JOIN user U ON S.assigned_to=U.id " +
                        "WHERE E.created_user = %d AND is_paid = false", userId);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getPendingPaymentsByGroup(int userId, String groupName) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', S.total_amount-S.paid_amount as '3' FROM expense E " +
                        "JOIN share S ON E.id=S.expense_id " +
                        "JOIN user U ON S.assigned_to=U.id " +
                        "JOIN grp G ON E.grp_id=G.id " +
                        "WHERE E.created_user = %d AND G.name='%s' AND is_paid = false", userId, groupName);
        return DB.conn.createStatement().executeQuery(sql);
    }

    static ResultSet getPendingPaymentsByUser(int userId, String userName) throws SQLException {
        String sql = String
                .format("SELECT E.name as '1', U.username as '2', S.total_amount-S.paid_amount as '3' FROM expense E " +
                        "JOIN share S ON E.id=S.expense_id " +
                        "JOIN user U ON S.assigned_to=U.id " +
                        "WHERE E.created_user = %d AND S.assigned_to='%s' AND is_paid = false", userId, userName);
        return DB.conn.createStatement().executeQuery(sql);
    }
}