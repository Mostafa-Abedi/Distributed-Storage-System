package server.data;

import interfaces.DataService;
import interfaces.MetadataService;

import java.io.*;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DataServer extends UnicastRemoteObject implements DataService {

    private final String serverName; // Unique identifier for this data server
    private final MetadataService metadataService; // Reference to Metadata Server
    private final File storageDirectory; // Directory for storing files locally

    public DataServer(String serverName, MetadataService metadataService) throws RemoteException {
        super();
        this.serverName = serverName;
        this.metadataService = metadataService;
        this.storageDirectory = new File("data/" + serverName);

        // Create storage directory if it doesn't exist
        if (!storageDirectory.exists() && !storageDirectory.mkdirs()) {
            throw new RuntimeException("Failed to create storage directory for " + serverName);
        }
    }

    @Override
    public boolean storeFile(String fileName, byte[] content, String owner) throws RemoteException {
        try {
            File file = new File(storageDirectory, fileName);

            // Write file data to local storage
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content);
            }

            // Register the file with the Metadata Server
            boolean registered = metadataService.registerFile(fileName, serverName, owner);
            if (registered) {
                System.out.println("Stored and registered file: " + fileName);
            } else {
                System.out.println("File already registered: " + fileName);
            }
            return registered;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] fetchFile(String fileName) throws RemoteException {
        try {
            File file = new File(storageDirectory, fileName);
            if (!file.exists()) {
                System.out.println("File not found: " + fileName);
                return null;
            }
            System.out.println("Fetching file: " + fileName);
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteFile(String fileName) throws RemoteException {
        File file = new File(storageDirectory, fileName);

        // Delete file locally
        if (file.exists() && file.delete()) {
            // Deregister file from Metadata Server
            boolean deregistered = metadataService.deleteFile(fileName);
            if (deregistered) {
                System.out.println("Deleted and deregistered file: " + fileName);
            }
            return deregistered;
        }
        System.out.println("File not found for deletion: " + fileName);
        return false;
    }
}
