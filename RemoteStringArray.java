import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteStringArray extends Remote {

    void insertArrayElement (int l, String str);

    boolean requestReadLock (int l, int client_id);
    
    boolean requestWriteLock (int l, int client_id);

    void releaseLock(int l, int client_id);

    String fetchElementRead(int l, int client_id) throws RemoteException;

    String fetchElementWrite(int l, int client_id);

    boolean writeBackElement (String str, int l, int client_id);

}