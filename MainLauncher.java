import server.data.DataServer;
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

            // Start multiple Data Servers
            String[] dataServerNames = {"DataServer1", "DataServer2", "DataServer3"};
            for (String serverName : dataServerNames) {
                DataServer dataServer = new DataServer(serverName, metadataServer);
                Naming.rebind(serverName, dataServer);
                System.out.println(serverName + " is running...");
            }

            // Start Client UI
            javax.swing.SwingUtilities.invokeLater(ClientUI::new);

        } catch (Exception e) {
            System.err.println("An error occurred while launching the system:");
            e.printStackTrace();
        }
    }
}