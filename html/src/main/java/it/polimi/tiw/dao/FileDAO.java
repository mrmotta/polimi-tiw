package it.polimi.tiw.dao;

import it.polimi.tiw.beans.File;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileDAO {
    private final Connection connection;

    public FileDAO(Connection connection) {
        this.connection = connection;
    }

    public List<File> getFilesFromFolder(int folder) throws SQLException {

        List<File> res = new ArrayList<>();
        String query = "SELECT  file.id, file.name, file.extension, file.parent, folder.user, file.creation, file.summary, file.type " +
                "FROM file JOIN folder ON file.parent = folder.id " +
                "WHERE file.parent = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setInt(1, folder);

            try (ResultSet result = pstatement.executeQuery()) {

                if (result.isBeforeFirst())
                    while (result.next())
                        res.add(new File(
                                result.getInt("file.id"),
                                result.getString("file.name"),
                                result.getString("file.extension"),
                                result.getInt("file.parent"),
                                result.getInt("folder.user"),
                                result.getDate("file.creation"),
                                result.getString("file.summary"),
                                result.getString("file.type")
                        ));
            }
        }

        return res;
    }

    public File getFile(int id) throws SQLException {

        File res = null;
        String query = "SELECT  file.id, file.name, file.extension, file.parent, file.creation, file.summary, file.type, folder.user " +
                "FROM file JOIN folder ON file.parent = folder.id " +
                "WHERE file.id = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setInt(1, id);

            try (ResultSet result = pstatement.executeQuery()) {

                if (result.isBeforeFirst())
                    while (result.next())
                        res = new File(
                                result.getInt("file.id"),
                                result.getString("file.name"),
                                result.getString("file.extension"),
                                result.getInt("file.parent"),
                                result.getInt("folder.user"),
                                result.getDate("file.creation"),
                                result.getString("file.summary"),
                                result.getString("file.type")
                        );
            }
        }

        return res;
    }


    public void createFile(User user, String name, String ext, int parent, Date creation, String summary,
                           String type) throws SQLException {
        String query = "INSERT INTO file (name, extension, parent, creation, summary, type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setString(1, name);
            pstatement.setString(2, ext);
            pstatement.setInt(3, parent);
            pstatement.setDate(4, new java.sql.Date(creation.getTime()));
            pstatement.setString(5, summary);
            pstatement.setString(6, type);

            pstatement.executeUpdate();
        }
    }

    public boolean exists(String name, String ext, int parent) throws SQLException {
        boolean check = false;
        String query = "SELECT * FROM file WHERE name = ? AND extension = ? AND parent = ?";

        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setString(1, name);
            pstatement.setString(2, ext);
            pstatement.setInt(3, parent);

            try (ResultSet result = pstatement.executeQuery();) {

                if (result.isBeforeFirst())
                    check = true;
            }
        }

        return check;
    }

    public void moveFile(File file, Folder dest) throws SQLException {
        String query = "UPDATE file SET parent = ? WHERE name = ? AND extension = ? AND parent = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {

            pstatement.setInt(1, dest.id());
            pstatement.setString(2, file.name());
            pstatement.setString(3, file.extension());
            pstatement.setInt(4, file.parent());
            pstatement.executeUpdate();

        }
    }
}
