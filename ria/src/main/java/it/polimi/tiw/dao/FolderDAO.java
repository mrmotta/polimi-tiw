package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.excetions.WrongValuesException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FolderDAO {

    private final Connection connection;

    public FolderDAO(Connection connection) {
        this.connection = connection;
    }

    public Folder getFolder(int id) throws SQLException {

        String query = "SELECT f1.name, f1.user, f1.parent, f2.id, f2.name, f2.parent " +
                "FROM folder AS f1 LEFT JOIN folder as f2 ON f1.id = f2.parent " +
                "WHERE f1.id = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setInt(1, id);

            try (ResultSet result = pstatement.executeQuery()) {

                String name = null;
                int user = -1;
                int parent = -1;
                List<Folder> subfolders = new ArrayList<>();

                if (result.isBeforeFirst()) {
                    while (result.next()) {

                        if (name == null) {
                            name = result.getString("f1.name");
                            user = result.getInt("f1.user");
                            parent = result.getInt("f1.parent");
                            if (parent == 0)
                                parent = -1;
                        }

                        if (result.getInt("f2.parent") != 0)
                            subfolders.add(new Folder(
                                    result.getInt("f2.id"),
                                    result.getString("f2.name"),
                                    user,
                                    parent,
                                    null
                            ));
                    }

                    return new Folder(id, name, user, parent, subfolders);
                }
            }
        }
        return null;
    }

    public List<Folder> getFoldersFromUser(User user) throws SQLException {
        String query = "SELECT f1.id, f1.name, f1.user, f1.parent, f2.id, f2.name, f2.parent " +
                "FROM folder AS f1 LEFT JOIN folder as f2 ON f1.id = f2.parent " +
                "WHERE f1.user = ? AND f1.parent IS NULL";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setInt(1, user.id());

            try (ResultSet result = pstatement.executeQuery()) {

                List<Folder> folders = new ArrayList<>();
                int id;
                String name;
                List<Folder> subfolders = new ArrayList<>();

                if (result.isBeforeFirst()) {
                    result.next();
                    do {

                        id = result.getInt("f1.id");
                        name = result.getString("f1.name");

                        do {
                            if (result.getInt("f2.id") != 0)
                                subfolders.add(new Folder(
                                        result.getInt("f2.id"),
                                        result.getString("f2.name"),
                                        user.id(),
                                        id,
                                        null
                                ));
                        } while (result.next() && result.getInt("f2.parent") == id);

                        folders.add(new Folder(id, name, user.id(), -1, subfolders));
                        subfolders.clear();

                    } while (!result.isAfterLast());

                    return folders;
                }
            }
        }
        return List.of();
    }

    public boolean exists(String name, int parent) throws SQLException {

        String query;

        if (parent == -1)
            query = "SELECT id, name FROM folder WHERE parent is null and name = ?";
        else
            query = "SELECT id, name FROM folder WHERE name = ? and parent = ? ";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setString(1, name);

            if (parent != -1)
                pstatement.setInt(2, parent);

            try (ResultSet result = pstatement.executeQuery()) {
                return result.isBeforeFirst();
            }
        }
    }

    public void createFolder(int user, String name, int parent, Date creation) throws SQLException, WrongValuesException {

        String query = "INSERT INTO folder (user, name, parent, creation) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setInt(1, user);
            pstatement.setString(2, name);

            if (parent == -1) {
                pstatement.setNull(3, Types.INTEGER);
            } else {
            	Folder folder = getFolder(parent);
                if (folder != null && folder.owner() == user && !folder.isSubfolder())
                    pstatement.setInt(3, parent);
                else {
                    throw new WrongValuesException("Parent and user owner mismatch");
                }

            }
            pstatement.setDate(4, new java.sql.Date(creation.getTime()));

            pstatement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM folder WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setInt(1, id);
            pstatement.executeUpdate();
        }
    }
}
