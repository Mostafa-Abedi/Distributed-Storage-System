package server.data;

import interfaces.MetadataService;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class DataServerMain {
    public static void main(String[] args) {
        try {
            // Connect to Metadata Server
            MetadataService metadataService = (MetadataService) Naming.lookup("rmi://localhost/MetadataServer");

            // Create a unique Data Server instance
            String serverName = "DataServer1"; // Change to DataServer2, DataServer3, etc. for other instances
            DataServer dataServer = new DataServer(serverName, metadataService);

            // Bind the Data Server to RMI Registry
            Naming.rebind(serverName, dataServer);

            System.out.println(serverName + " is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
