package assignment.adcs;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteStringArray extends Remote {

    public void insertArrayElement(int l, String str) throws RemoteException;

    public boolean requestReadLock(int l, int client_id) throws RemoteException;
    
    public boolean requestWriteLock(int l, int client_id) throws RemoteException;

    public void releaseLock(int l, int client_id) throws RemoteException;

    public String fetchElementRead(int l, int client_id) throws RemoteException;

    public String fetchElementWrite(int l, int client_id) throws RemoteException;

    public boolean writeBackElement(String str, int l, int client_id) throws RemoteException;

    public void releaseReadLock(int l, int client_id) throws RemoteException;

    public int size() throws RemoteException;

    public String getAllReadLocks() throws RemoteException;

    public String getAllWriteLocks() throws RemoteException;

}