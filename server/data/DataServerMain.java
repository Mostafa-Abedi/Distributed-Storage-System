package server.data;

import interfaces.MetadataService;

import javax.swing.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DataServerMain {
    public static void main(String[] args) {
        try {
            // Start the RMI registry
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

            // Lookup MetadataService
            MetadataService metadataService = (MetadataService) Naming.lookup("rmi://localhost/MetadataServer");

            // Create and bind DataServer
            DataServer dataServer = new DataServer(serverName, storagePath, metadataService);
            Naming.rebind("rmi://localhost/" + serverName, dataServer);

            System.out.println("DataServer " + serverName + " initialized at storage path: " + storagePath);
            JOptionPane.showMessageDialog(null, "DataServer " + serverName + " is running and registered.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to start DataServer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
