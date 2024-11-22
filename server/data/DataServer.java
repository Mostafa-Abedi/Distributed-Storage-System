package server.data;

import interfaces.DataService;
import interfaces.MetadataService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataServer extends UnicastRemoteObject implements DataService {
    private final String serverName;
    private final String storagePath; // Directory for storing files
    private final MetadataService metadataService;

    public DataServer(String serverName, String storagePath, MetadataService metadataService) throws RemoteException {
        super();
        this.serverName = serverName;
        this.storagePath = storagePath;
        this.metadataService = metadataService;

        // Ensure the storage directory exists
        File storageDir = new File(storagePath);
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new RemoteException("Failed to create storage directory at: " + storagePath);
        }
        System.out.println("DataServer initialized with storage path: " + storagePath);
    }

    @Override
    public boolean storeFile(String fileName, byte[] content, String owner) throws RemoteException {
        try {
            File file = new File(storagePath, fileName);

            // Write the file content to the storage directory
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content);
            }

            System.out.println("File stored: " + fileName + " at " + storagePath);

            // Register the file with the MetadataServer
            metadataService.registerFile(fileName, serverName, owner, null); // Public by default
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] fetchFile(String fileName) throws RemoteException {
        try {
            File file = new File(storagePath, fileName);

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
        File file = new File(storagePath, fileName);

        if (file.exists() && file.delete()) {
            System.out.println("File deleted: " + fileName);
            return true;
        }

        System.out.println("File not found for deletion: " + fileName);
        return false;
    }

    @Override
    public List<String> listFiles() throws RemoteException {
        List<String> files = new ArrayList<>();
        File storageDir = new File(storagePath);
    
        if (storageDir.exists() && storageDir.isDirectory()) {
            for (File file : Objects.requireNonNull(storageDir.listFiles())) {
                if (file.isFile()) {
                    files.add(file.getName());
                }
            }
        }
    
        return files;
    }    
    
    
}
