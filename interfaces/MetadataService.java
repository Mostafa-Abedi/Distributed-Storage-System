package interfaces;

import server.metadata.FileMetadata;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface MetadataService extends Remote {
    boolean createUser(String username, String password) throws RemoteException;

    boolean authenticateUser(String username, String password) throws RemoteException;

    boolean registerFile(String fileName, String dataServer, String owner, String permissions) throws RemoteException;

    String locateFile(String fileName) throws RemoteException;

    boolean deleteFile(String fileName) throws RemoteException;

    boolean hasPermission(String username, String fileName) throws RemoteException;

    Map<String, FileMetadata> listFiles() throws RemoteException;

    boolean registerDataServer(String serverName, String location, String owner) throws RemoteException;

    List<Map<String, String>> listDataServers() throws RemoteException; // No generics issue here

    String getStoragePath(String serverName) throws RemoteException;
}
