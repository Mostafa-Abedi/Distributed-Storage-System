import server.metadata.MetadataServer;
import client.ClientUI;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
}
