package server.metadata;

import interfaces.MetadataService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.*;

public class MetadataServer extends UnicastRemoteObject implements MetadataService {
    private final Connection connection;

    public MetadataServer() throws RemoteException {
        super();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:metadata.db");
            initializeDatabase();
        } catch (Exception e) {
            throw new RemoteException("Failed to initialize MetadataServer.", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL
                );
                """;

        String createFilesTable = """
                CREATE TABLE IF NOT EXISTS file_metadata (
                    file_name TEXT PRIMARY KEY,
                    data_server TEXT NOT NULL,
                    owner TEXT NOT NULL,
                    permissions TEXT
                );
                """;

        String createDataServersTable = """
                CREATE TABLE IF NOT EXISTS data_servers (
                    server_name TEXT PRIMARY KEY,
                    location TEXT NOT NULL,
                    owner TEXT NOT NULL
                );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createFilesTable);
            stmt.execute(createDataServersTable);
        }
    }

    @Override
    public synchronized boolean createUser(String username, String password) throws RemoteException {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            System.out.println("User created: " + username);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to create user: " + username);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean authenticateUser(String username, String password) throws RemoteException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Failed to authenticate user: " + username);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean registerFile(String fileName, String dataServer, String owner, String permissions) throws RemoteException {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT OR IGNORE INTO file_metadata (file_name, data_server, owner, permissions) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, fileName);
            pstmt.setString(2, dataServer);
            pstmt.setString(3, owner);
            pstmt.setString(4, permissions == null || permissions.isEmpty() ? null : permissions);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public synchronized Map<String, FileMetadata> listFiles() throws RemoteException {
        Map<String, FileMetadata> files = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM file_metadata")) {
            while (rs.next()) {
                files.put(rs.getString("file_name"),
                        new FileMetadata(rs.getString("data_server"), rs.getString("owner"), rs.getString("permissions")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }
    

    @Override
    public synchronized String locateFile(String fileName) throws RemoteException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT data_server FROM file_metadata WHERE file_name = ?")) {
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("data_server") : null;
        } catch (SQLException e) {
            System.err.println("Failed to locate file: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized boolean deleteFile(String fileName) throws RemoteException {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM file_metadata WHERE file_name = ?")) {
            pstmt.setString(1, fileName);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("File deleted: " + fileName);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Failed to delete file: " + fileName);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean hasPermission(String username, String fileName) throws RemoteException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT owner, permissions FROM file_metadata WHERE file_name = ?")) {
            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String owner = rs.getString("owner");
                String permissions = rs.getString("permissions");
    
                // Allow if the user is the owner
                if (owner.equals(username)) {
                    return true;
                }
    
                // Allow if the permissions are null (public) or contain the username
                return permissions == null || permissions.isEmpty() || permissions.contains(username);
            }
        } catch (SQLException e) {
            System.err.println("Failed to check permissions for file: " + fileName);
            e.printStackTrace();
        }
        return false;
    }
    



    @Override
    public synchronized boolean registerDataServer(String serverName, String location, String owner) throws RemoteException {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO data_servers (server_name, location, owner) VALUES (?, ?, ?)")) {
            pstmt.setString(1, serverName);
            pstmt.setString(2, location);
            pstmt.setString(3, owner);
            pstmt.executeUpdate();
            System.out.println("DataServer registered: " + serverName);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to register DataServer: " + serverName);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized String getStoragePath(String serverName) throws RemoteException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT location FROM data_servers WHERE server_name = ?")) {
            pstmt.setString(1, serverName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("location") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized List<Map<String, String>> listDataServers() throws RemoteException {
        List<Map<String, String>> servers = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM data_servers")) {
            while (rs.next()) {
                Map<String, String> server = new HashMap<>();
                server.put("name", rs.getString("server_name"));
                server.put("location", rs.getString("location"));
                server.put("owner", rs.getString("owner"));
                servers.add(server);
            }
        } catch (SQLException e) {
            System.err.println("Failed to list DataServers.");
            e.printStackTrace();
        }
        return servers;
    }
}
