package client;

import interfaces.DataService;
import interfaces.MetadataService;
import server.metadata.FileMetadata;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.rmi.Naming;
import java.util.Map;

public class ClientUI {
    private String username;
    private String metadataServerURL;
    private MetadataService metadataService;

    private JFrame mainFrame;
    private DefaultListModel<String> fileListModel;

    public ClientUI() {
        initializeConnectionUI();
    }

    private void initializeConnectionUI() {
        JFrame connectionFrame = new JFrame("Connect to Metadata Server");
        connectionFrame.setSize(400, 300);
        connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectionFrame.setLayout(new GridLayout(5, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();

        JLabel rmiPortLabel = new JLabel("RMI Port:");
        JTextField rmiPortField = new JTextField("1099");

        JLabel dataServerLabel = new JLabel("Data Server Name:");
        JTextField dataServerField = new JTextField("DataServer1");

        JButton connectButton = new JButton("Connect");

        connectionFrame.add(usernameLabel);
        connectionFrame.add(usernameField);
        connectionFrame.add(rmiPortLabel);
        connectionFrame.add(rmiPortField);
        connectionFrame.add(dataServerLabel);
        connectionFrame.add(dataServerField);
        connectionFrame.add(new JLabel());
        connectionFrame.add(connectButton);

        connectionFrame.setVisible(true);

        connectButton.addActionListener(e -> {
            username = usernameField.getText().trim();
            String rmiPort = rmiPortField.getText().trim();
            String dataServerName = dataServerField.getText().trim();

            if (username.isEmpty() || rmiPort.isEmpty() || dataServerName.isEmpty()) {
                JOptionPane.showMessageDialog(connectionFrame, "Please fill out all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                metadataServerURL = "rmi://localhost:" + rmiPort + "/MetadataServer";
                metadataService = (MetadataService) Naming.lookup(metadataServerURL);
                JOptionPane.showMessageDialog(connectionFrame, "Connected successfully!");
                connectionFrame.dispose();
                initializeMainUI(dataServerName);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(connectionFrame, "Failed to connect to Metadata Server.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }

    private void initializeMainUI(String dataServerName) {
        mainFrame = new JFrame("Distributed File System - " + username);
        mainFrame.setSize(600, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        fileListModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(fileListModel);
        JScrollPane fileScrollPane = new JScrollPane(fileList);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton refreshButton = new JButton("Refresh");
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        JButton deleteButton = new JButton("Delete");

        buttonPanel.add(refreshButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(deleteButton);

        mainFrame.add(fileScrollPane, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshFileList());
        uploadButton.addActionListener(e -> uploadFile(dataServerName));
        downloadButton.addActionListener(e -> downloadFile(fileList.getSelectedValue(), dataServerName));
        deleteButton.addActionListener(e -> deleteFile(fileList.getSelectedValue(), dataServerName));

        refreshFileList();

        mainFrame.setVisible(true);
    }

    private void refreshFileList() {
        try {
            fileListModel.clear();
            Map<String, FileMetadata> files = metadataService.listFiles();
            for (Map.Entry<String, FileMetadata> entry : files.entrySet()) {
                String fileName = entry.getKey();
                FileMetadata metadata = entry.getValue();
                fileListModel.addElement("Name: " + fileName + " | Location: " + metadata.getDataServer() + " | Uploaded by: " + metadata.getOwner());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Failed to refresh file list.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void uploadFile(String dataServerName) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(mainFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                DataService dataService = (DataService) Naming.lookup("rmi://localhost/" + dataServerName);

                boolean success = dataService.storeFile(file.getName(), content, username);
                if (success) {
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

    private void downloadFile(String selectedFile, String dataServerName) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a file to download.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fileName = selectedFile.split(" \\| ")[0].substring(6); // Extract file name from display
        try {
            DataService dataService = (DataService) Naming.lookup("rmi://localhost/" + dataServerName);
            byte[] content = dataService.fetchFile(fileName);
            if (content != null) {
                String contentStr = new String(content);
                JOptionPane.showMessageDialog(mainFrame, "File content:\n" + contentStr, "Download Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Failed to download file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Error connecting to Data Server.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteFile(String selectedFile, String dataServerName) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(mainFrame, "Please select a file to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fileName = selectedFile.split(" \\| ")[0].substring(6); // Extract file name from display
        try {
            DataService dataService = (DataService) Naming.lookup("rmi://localhost/" + dataServerName);
            boolean success = dataService.deleteFile(fileName);
            if (success) {
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
