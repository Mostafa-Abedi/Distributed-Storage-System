package server.metadata;

import java.rmi.Naming;

public class MetadataServerMain {
    public static void main(String[] args) {
        try {
            MetadataServer server = new MetadataServer();
            Naming.rebind("MetadataServer", server);
            System.out.println("MetadataServer is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
