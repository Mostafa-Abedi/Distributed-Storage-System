import server.metadata.MetadataServer;
import server.data.DataServer;
import client.ClientUI;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

public class MainLauncher {

    public static void main(String[] args) {
        try {
            // Check if RMI Registry is already running
            if (!isRmiRegistryRunning()) {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
                System.out.println("RMI Registry started.");
            } else {
                System.out.println("RMI Registry is already running.");
            }

            // Start Metadata Server
            MetadataServer metadataServer = new MetadataServer();
            Naming.rebind("MetadataServer", metadataServer);
            System.out.println("MetadataServer is running...");

            // Initialize all registered DataServers
            initializeExistingDataServers(metadataServer);

            // Launch Client UI
            javax.swing.SwingUtilities.invokeLater(ClientUI::new);

        } catch (Exception e) {
            System.err.println("An error occurred while launching the system:");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the RMI registry is already running.
     * 
     * @return true if the registry is running, false otherwise.
     */
    private static boolean isRmiRegistryRunning() {
        try {
            Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
            registry.list(); // Attempt to list bound objects
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initializes all existing DataServers registered in the MetadataServer.
     * 
     * @param metadataServer The MetadataServer instance to fetch DataServers from.
     */
    private static void initializeExistingDataServers(MetadataServer metadataServer) {
        try {
            // Fetch all registered DataServers from metadata
            List<Map<String, String>> servers = metadataServer.listDataServers();

            for (Map<String, String> server : servers) {
                String serverName = server.get("name");
                String storagePath = server.get("location");

                // Check if the DataServer is already running
                try {
                    Naming.lookup("rmi://localhost/" + serverName);
                    System.out.println("DataServer " + serverName + " is already running.");
                } catch (Exception e) {
                    // DataServer is not running; initialize it
                    DataServer dataServer = new DataServer(serverName, storagePath, metadataServer);
                    Naming.rebind("rmi://localhost/" + serverName, dataServer);
                    System.out.println("DataServer " + serverName + " started with storage at " + storagePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize existing DataServers:");
            e.printStackTrace();
        }
    }
}
