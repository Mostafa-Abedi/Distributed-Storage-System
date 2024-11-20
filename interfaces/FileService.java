package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FileService extends Remote {
    boolean createFile(String fileName, String owner) throws RemoteException;
    byte[] readFile(String fileName) throws RemoteException;
    boolean deleteFile(String fileName) throws RemoteException;
    boolean storeFile(String fileName, byte[] content) throws RemoteException;
    List<String> listFiles() throws RemoteException;  // Add import for List
}
