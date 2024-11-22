package server.data;

import interfaces.MetadataService;

import javax.swing.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.File;

public class DataServerMain {
    public static void main(String[] args) {
        try {
            // Start the RMI registry if not already running
            try {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
                System.out.println("RMI registry started.");
            } catch (Exception e) {
                System.out.println("RMI registry already running.");
            }

            // Input for server name
            String serverName = JOptionPane.showInputDialog("Enter Data Server Name:");
            if (serverName == null || serverName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Server name cannot be empty.");
                return;
            }

            // Input for storage path
            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = folderChooser.showOpenDialog(null);

            if (result != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(null, "No folder selected. DataServer creation canceled.");
                return;
            }

            String storagePath = folderChooser.getSelectedFile().getAbsolutePath();

            // Validate storage path
            File storageDir = new File(storagePath);
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                JOptionPane.showMessageDialog(null, "Failed to create storage directory at: " + storagePath, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("Validated storage directory at: " + storagePath);

            // Lookup MetadataService
            MetadataService metadataService;
            try {
                metadataService = (MetadataService) Naming.lookup("rmi://localhost/MetadataServer");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to connect to MetadataServer. Ensure it is running.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }

            // Create and bind DataServer
            try {
                DataServer dataServer = new DataServer(serverName, storagePath, metadataService);
                Naming.rebind("rmi://localhost/" + serverName, dataServer);
                System.out.println("DataServer " + serverName + " initialized at storage path: " + storagePath);

                // Notify MetadataServer about the new DataServer
                boolean registered = metadataService.registerDataServer(serverName, storagePath, "system"); // Replace "system" with the appropriate owner
                if (registered) {
                    JOptionPane.showMessageDialog(null, "DataServer " + serverName + " is running and registered successfully.");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to register DataServer with MetadataServer.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to start DataServer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unexpected error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
