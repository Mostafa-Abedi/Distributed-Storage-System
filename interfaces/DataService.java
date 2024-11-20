package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataService extends Remote {
    boolean storeFile(String fileName, byte[] content, String owner) throws RemoteException;
    byte[] fetchFile(String fileName) throws RemoteException;
    boolean deleteFile(String fileName) throws RemoteException;
}
