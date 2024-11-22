package client;

import interfaces.DataService;
import interfaces.MetadataService;
import server.metadata.FileMetadata;
import server.data.DataServer;
import server.data.DataServerMain;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.List;



public class ClientUI {
    private String username;
    private String connectedDataServer;
    private MetadataService metadataService;

    private JFrame mainFrame;
    private DefaultListModel<String> fileListModel;

    public ClientUI() {
        initializeConnectionUI();
    }

    // User Authentication UI
    private void initializeConnectionUI() {
        JFrame connectionFrame = new JFrame("User Authentication");
        connectionFrame.setSize(400, 300);
        connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectionFrame.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Log In");
        JButton createUserButton = new JButton("Create User");

        connectionFrame.add(usernameLabel);
        connectionFrame.add(usernameField);
        connectionFrame.add(passwordLabel);
        connectionFrame.add(passwordField);
        connectionFrame.add(loginButton);
        connectionFrame.add(createUserButton);

        connectionFrame.setVisible(true);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(connectionFrame, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                metadataService = (MetadataService) Naming.lookup("rmi://localhost/MetadataServer");
                boolean authenticated = metadataService.authenticateUser(user, pass);
                if (authenticated) {
                    username = user;
                    JOptionPane.showMessageDialog(connectionFrame, "Login successful!");
                    connectionFrame.dispose();
                    initializeDataServerSelectionUI();
                } else {
                    JOptionPane.showMessageDialog(connectionFrame, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(connectionFrame, "Error connecting to Metadata Server.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        createUserButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(connectionFrame, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                metadataService = (MetadataService) Naming.lookup("rmi://localhost/MetadataServer");
                boolean created = metadataService.createUser(user, pass);
                if (created) {
                    JOptionPane.showMessageDialog(connectionFrame, "User created successfully! Please log in.");
                } else {
                    JOptionPane.showMessageDialog(connectionFrame, "User creation failed. Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(connectionFrame, "Error connecting to Metadata Server.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }

    // Data Server Selection UI
    private void initializeDataServerSelectionUI() {
        JFrame serverFrame = new JFrame("Select or Create Data Server");
        serverFrame.setSize(600, 400);
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.setLayout(new BorderLayout());

        // List of available Data Servers
        DefaultListModel<String> serverListModel = new DefaultListModel<>();
        JList<String> serverList = new JList<>(serverListModel);
        JScrollPane serverScrollPane = new JScrollPane(serverList);

        try {
            List<Map<String, String>> servers = metadataService.listDataServers();
            for (Map<String, String> server : servers) {
                serverListModel.addElement("Name: " + server.get("name") +
                        " | Location: " + server.get("location") +
                        " | Owner: " + server.get("owner"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(serverFrame, "Failed to fetch server list.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        JButton selectServerButton = new JButton("Select Server");
        JButton createServerButton = new JButton("Create Server");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(selectServerButton);
        buttonPanel.add(createServerButton);

        serverFrame.add(serverScrollPane, BorderLayout.CENTER);
        serverFrame.add(buttonPanel, BorderLayout.SOUTH);

        serverFrame.setVisible(true);

        selectServerButton.addActionListener(e -> {
            String selected = serverList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(serverFrame, "Please select a server.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            // Extract the server name from the selected value
            connectedDataServer = selected.split("\\|")[0].split(":")[1].trim();
        
            try {
                // Lookup MetadataService
                MetadataService metadataService = (MetadataService) Naming.lookup("rmi://localhost/MetadataServer");
        
                // Retrieve the storage path from metadata
                String storagePath = metadataService.getStoragePath(connectedDataServer); // Ensure getStoragePath is implemented in MetadataService
        
                if (storagePath == null || storagePath.isEmpty()) {
                    JOptionPane.showMessageDialog(serverFrame, "Storage path not found for the selected server.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
        
                // Initialize the DataServer
                DataServer dataServer = new DataServer(connectedDataServer, storagePath, metadataService);
                Naming.rebind("rmi://localhost/" + connectedDataServer, dataServer);
        
                JOptionPane.showMessageDialog(serverFrame, "DataServer " + connectedDataServer + " initialized with storage at " + storagePath + " and started.");
                serverFrame.dispose();
                initializeMainUI();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(serverFrame, "Error initializing DataServer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
              

        createServerButton.addActionListener(e -> {
            JTextField serverNameField = new JTextField();
            JButton browseButton = new JButton("Browse");
            JLabel folderLabel = new JLabel("No folder selected");
        
            browseButton.addActionListener(evt -> {
                JFileChooser folderChooser = new JFileChooser();
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = folderChooser.showOpenDialog(serverFrame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    folderLabel.setText(folderChooser.getSelectedFile().getAbsolutePath());
                }
            });
        
            Object[] fields = {
                    "Server Name:", serverNameField,
                    "Data Location:", folderLabel,
                    browseButton
            };
        
            int result = JOptionPane.showConfirmDialog(serverFrame, fields, "Create Data Server", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String serverName = serverNameField.getText().trim();
                String serverLocation = folderLabel.getText();
        
                if (serverName.isEmpty() || "No folder selected".equals(serverLocation)) {
                    JOptionPane.showMessageDialog(serverFrame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
        
                try {
                    // Instantiate and bind DataServer
                    DataServer dataServer = new DataServer(serverName, serverLocation, metadataService);
                    java.rmi.registry.LocateRegistry.getRegistry().rebind(serverName, dataServer);
        
                    // Add DataServer details to the metadata service
                    boolean metadataUpdated = metadataService.registerDataServer(serverName, serverLocation, username);
        
                    if (!metadataUpdated) {
                        JOptionPane.showMessageDialog(serverFrame, "Failed to update metadata service with DataServer details.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
        
                    // Add to UI list and notify user
                    serverListModel.addElement("Name: " + serverName +
                            " | Location: " + serverLocation +
                            " | Owner: " + username);
                    JOptionPane.showMessageDialog(serverFrame, "Data Server created and registered successfully!");
        
                    System.out.println("DataServer " + serverName + " registered successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(serverFrame, "Error creating Data Server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
    }        

    // Main File Management UI
    private void initializeMainUI() {
        mainFrame = new JFrame("Distributed File System - " + username);
        mainFrame.setSize(600, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel serverLabel = new JLabel("Connected to: " + connectedDataServer);
        JButton logoutButton = new JButton("Logout");
        JButton switchServerButton = new JButton("Switch Server");

        logoutButton.addActionListener(e -> {
            mainFrame.dispose();
            initializeConnectionUI();
        });

        switchServerButton.addActionListener(e -> {
            mainFrame.dispose();
            initializeDataServerSelectionUI();
        });

        topPanel.add(serverLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        topPanel.add(switchServerButton, BorderLayout.WEST);

        fileListModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(fileListModel);
        JScrollPane fileScrollPane = new JScrollPane(fileList);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        JButton deleteButton = new JButton("Delete");

        buttonPanel.add(refreshButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(deleteButton);

        mainFrame.add(topPanel, BorderLayout.NORTH);
        mainFrame.add(fileScrollPane, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshFileList());
        uploadButton.addActionListener(e -> uploadFile());
        downloadButton.addActionListener(e -> downloadFile(fileList.getSelectedValue()));
        deleteButton.addActionListener(e -> deleteFile(fileList.getSelectedValue()));

        refreshFileList();

        mainFrame.setVisible(true);
    }
    
    private void refreshFileList() {
        try {
            fileListModel.clear();
    
            // Lookup and cast the connected DataServer to DataService
            DataService dataService = (DataService) Naming.lookup("rmi://localhost/" + connectedDataServer);
            MetadataService metadataService = (MetadataService) Naming.lookup("rmi://localhost/MetadataServer");
    
            // Fetch files from the storage directory and metadata
            List<String> directoryFiles = dataService.listFiles();
            Map<String, FileMetadata> metadataFiles = metadataService.listFiles();
    
            for (String fileName : directoryFiles) {
                if (!metadataFiles.containsKey(fileName)) {
                    // Add missing files to the metadata with public permissions
                    metadataService.registerFile(fileName, connectedDataServer, "public", null);
                }
                fileListModel.addElement("File: " + fileName + " | Server: " + connectedDataServer);
            }
    
            JOptionPane.showMessageDialog(mainFrame, "File list refreshed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Failed to refresh file list.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    
    
    
    
    
    
    

    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(mainFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                DataService dataService;

                try {
                    dataService = (DataService) Naming.lookup("rmi://localhost/" + connectedDataServer);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainFrame, "DataServer not found. Please ensure it's running.", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }

                String permissions = JOptionPane.showInputDialog(mainFrame, "Enter permissions (comma-separated usernames, or leave blank for public):");
                boolean success = dataService.storeFile(file.getName(), content, username);
                if (success) {
                    metadataService.registerFile(file.getName(), connectedDataServer, username, permissions);
                    refreshFileList();
                    JOptionPane.showMessageDialog(mainFrame, "File uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "File already exists or upload failed.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Failed to upload file.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void downloadFile(String selectedFile) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a file to download.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fileName = selectedFile.split(" \\| ")[0].substring(6); // Extract file name from display
        try {
            if (!metadataService.hasPermission(username, fileName)) {
                JOptionPane.showMessageDialog(mainFrame, "You do not have permission to download this file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DataService dataService = (DataService) Naming.lookup("rmi://localhost/" + connectedDataServer);
            byte[] content = dataService.fetchFile(fileName);
            if (content != null) {
                JFileChooser saveChooser = new JFileChooser();
                saveChooser.setSelectedFile(new File(fileName));
                int saveResult = saveChooser.showSaveDialog(mainFrame);
                if (saveResult == JFileChooser.APPROVE_OPTION) {
                    Files.write(saveChooser.getSelectedFile().toPath(), content);
                    JOptionPane.showMessageDialog(mainFrame, "File downloaded successfully!", "Download Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Failed to download file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Error connecting to Data Server.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteFile(String selectedFile) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a file to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fileName = selectedFile.split(" \\| ")[0].substring(6); // Extract file name from display
        try {
            if (!username.equals(metadataService.listFiles().get(fileName).getOwner())) {
                JOptionPane.showMessageDialog(mainFrame, "You do not have permission to delete this file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DataService dataService = (DataService) Naming.lookup("rmi://localhost/" + connectedDataServer);
            boolean success = dataService.deleteFile(fileName);
            if (success) {
                metadataService.deleteFile(fileName);
                refreshFileList();
                JOptionPane.showMessageDialog(mainFrame, "File deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Failed to delete file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Error connecting to Data Server.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientUI::new);
    }
}
