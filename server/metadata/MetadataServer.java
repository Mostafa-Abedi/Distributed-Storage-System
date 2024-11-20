package server.metadata;

import interfaces.MetadataService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MetadataServer extends UnicastRemoteObject implements MetadataService {

    private final Connection connection;

    public MetadataServer() throws RemoteException {
        super();

        // Initialize SQLite database
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:metadata.db");
            initializeDatabase();
        } catch (SQLException e) {
            throw new RemoteException("Failed to connect to database", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        String createTableQuery = """
                CREATE TABLE IF NOT EXISTS file_metadata (
                    file_name TEXT PRIMARY KEY,
                    data_server TEXT NOT NULL,
                    owner TEXT NOT NULL
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableQuery);
        }
    }

    @Override
    public synchronized boolean registerFile(String fileName, String dataServer, String owner) throws RemoteException {
        String insertQuery = "INSERT INTO file_metadata (file_name, data_server, owner) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, fileName);
            pstmt.setString(2, dataServer);
            pstmt.setString(3, owner);
            pstmt.executeUpdate();
            System.out.println("Registered file: " + fileName + " on server: " + dataServer);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to register file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public synchronized String locateFile(String fileName) throws RemoteException {
        String selectQuery = "SELECT data_server FROM file_metadata WHERE file_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("data_server");
            }
        } catch (SQLException e) {
            System.err.println("Failed to locate file: " + e.getMessage());
        }
        return null;
    }

    @Override
    public synchronized boolean deleteFile(String fileName) throws RemoteException {
        String deleteQuery = "DELETE FROM file_metadata WHERE file_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Deleted metadata for file: " + fileName);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete file metadata: " + e.getMessage());
            return false;
        }
    }

    @Override
    public synchronized Map<String, FileMetadata> listFiles() throws RemoteException {
        String selectQuery = "SELECT * FROM file_metadata";
        Map<String, FileMetadata> files = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectQuery)) {
            while (rs.next()) {
                String fileName = rs.getString("file_name");
                String dataServer = rs.getString("data_server");
                String owner = rs.getString("owner");
                files.put(fileName, new FileMetadata(dataServer, owner));
            }
        } catch (SQLException e) {
            System.err.println("Failed to list files: " + e.getMessage());
        }
        return files;
    }
}
