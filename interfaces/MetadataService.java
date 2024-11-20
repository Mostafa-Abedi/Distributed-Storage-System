package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface MetadataService extends Remote {
    boolean registerFile(String fileName, String dataServer, String owner) throws RemoteException;
    String locateFile(String fileName) throws RemoteException;
    boolean deleteFile(String fileName) throws RemoteException;
    Map<String, FileMetadata> listFiles() throws RemoteException;
}
