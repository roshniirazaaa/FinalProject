package hospital.controller;

import hospital.db.DBConnection;
import hospital.model.User;

import java.sql.*;

public class UserController {

   public static User login(String username, String password) {
    User user = null;
    try {
        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM user WHERE username=? AND password=?"
        );
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            user = new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")  // important
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return user;
}


    public static boolean addUser(User u) {
        String sql = "INSERT INTO user(username, password, role) VALUES(?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}
