package it.polimi.tiw.dao;

import it.polimi.tiw.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public User checkCredentials(String username, String password) throws SQLException {

        String query = "SELECT  id, username FROM User WHERE username = ? AND password = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setString(1, username);
            pstatement.setString(2, password);

            try (ResultSet result = pstatement.executeQuery()) {

                if (result.isBeforeFirst()) {

                    result.next();

                    return new User(result.getInt("id"), result.getString("username"));
                }

                return null;
            }
        }
    }

    public boolean exists(String username) throws SQLException {

        String query = "SELECT id FROM User WHERE username = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setString(1, username);

            try (ResultSet result = pstatement.executeQuery()) {
                return result.isBeforeFirst();
            }
        }
    }

    public User createUser(String username, String email, String password) throws SQLException {

        String query = "INSERT INTO User (username, email, password) VALUES (?, ?, ?)";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setString(1, username);
            ;
            pstatement.setString(2, email);
            pstatement.setString(3, password);

            pstatement.executeUpdate();

            return checkCredentials(username, password);
        }
    }
}
