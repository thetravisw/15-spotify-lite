package Server.db;

import Server.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDB {
    private static Connection mConn;

    static {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/javaauth";

            try {
                mConn = DriverManager.getConnection(url);
                ResultSet results = mConn.createStatement().executeQuery("SELECT * FROM users");
                while (results.next()) {
                    int id = results.getInt("id");
                    String name = results.getString("username");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("DB error.");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Postgres driver not configured correctly.");
        }
    }

    public static void reset() {
        String sql = "DROP DATABASE IF EXISTS  javaauth; " +
        "CREATE DATABASE javaauth; " +
        "DROP TABLE IF EXISTS users; " +
        "CREATE TABLE users ( " +
        "        id serial, " +
        "        username text, " +
        "        passhash text, " +
        "); " +
        "INSERT INTO users(username, passhash) " +
        "VALUES('moonmayor', '$2a$12$u7s.Q60pWu01Yujt6KH4wuX8Dcf9Pm1PlwEoQcGXhHrpYzRH53.Se';";

        try {
            mConn.createStatement().execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static User createUser(String username, String password) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "INSERT INTO users(username, passhash) VALUES('%s', '%s') RETURNING (id);";
        sql = String.format(sql, username, hashed);

        try (ResultSet results = mConn.createStatement().executeQuery(sql)) {
            results.next();
            int id = results.getInt("id");
            User user = new User(id, username, hashed);
            return user;
        } catch (SQLException e) {
            return null;
        }
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (ResultSet results = mConn.createStatement().executeQuery(sql)) {
            while (results.next()) {
                int id = results.getInt("id");
                String username = results.getString("username");
                String passhash = results.getString("passhash");

                User user = new User(id, username, passhash);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static User getUserById(int searchId) {
        String sql = "SELECT * FROM users WHERE id=%d";
        sql = String.format(sql, searchId);

        try (ResultSet results = mConn.createStatement().executeQuery(sql)) {
            results.next();
            int id = results.getInt("id");
            String username = results.getString("username");
            String passhash = results.getString("passhash");

            User user = new User(id, username, passhash);
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserByName(String searchUsername) {
        String sql = "SELECT * FROM users WHERE username='%s'";
        sql = String.format(sql, searchUsername);

        try (ResultSet results = mConn.createStatement().executeQuery(sql)) {
            if (!results.next()) {
                // no matching user
                return null;
            }

            int id = results.getInt("id");
            String username = results.getString("username");
            String passhash = results.getString("passhash");

            User user = new User(id, username, passhash);
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean deleteUser (int searchId) {
        String sql = "DELETE FROM users WHERE id=%d;";
        sql = String.format(sql, searchId);

        try {
            mConn.createStatement().execute(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }
}
