import server.metadata.MetadataServer;
import client.ClientUI;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainLauncher {
    public static void main(String[] args) {
        try {
            // Start RMI Registry
            LocateRegistry.createRegistry(1099);
            System.out.println("RMI Registry started.");

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
}
