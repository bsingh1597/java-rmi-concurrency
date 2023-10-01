import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteStringArrayServer implements RemoteStringArray {

    String[] strArray;
    Map<Integer, List<Integer>> readLockMap = new HashMap<>();
    Map<Integer, Integer> writeLockMap = new HashMap<>();

    public RemoteStringArrayServer(int n) {
        strArray = new String[n];
    }

    @Override
    public boolean writeBackElement(String str, int l, int client_id) {
        // TODO Auto-generated method stub
        return false;
    }

    // Check for the read only lock for the client_id
    @Override
    public String fetchElementRead(int l, int client_id) {
        if (checkReadLock(client_id, l)) {
            return strArray[l];
        } else {
            throw new RuntimeException("No read Only Lock");
        }
    }

    // Check Read Lock on element for client_id
    // return true if lock is there
    private boolean checkReadLock(int client_id, int l) {

        List<Integer> clientList;
        clientList = readLockMap.get(l);
        if (clientList != null) {
            return clientList.contains(client_id);
        }
        return false;
    }

    // Check Read Lock on element 
    // return true if lock is there
    private boolean checkReadLock(int element) {
        if(readLockMap.get(element) != null && !readLockMap.get(element).isEmpty())
            return true;
        else
            return false;
    }

    // Check Read Lock on element 
    // return true if lock is there
    private boolean checkWriteLock(int element) {
        if (writeLockMap.get(element) != null) {
            return true;
        }
        return false;
    }

    @Override
    public String fetchElementWrite(int l, int client_id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertArrayElement(int l, String str) {
        // TODO - add the checks here
        strArray[l] = str;
    }

    // Release any read or write lock on the element for that Id
    @Override
    public void releaseLock(int l, int client_id) {

        try {
            if (writeLockMap.get(l) != l && writeLockMap.get(l) == client_id) {
                writeLockMap.remove(l);
            }
            if(readLockMap.get(l) != null) {
                readLockMap.get(l).remove(client_id);
            }
        } catch (RuntimeException e) {

        }

    }

    @Override
    public boolean requestReadLock(int l, int client_id) {
        try {
            // If write lock is there then no read lock
            if (!checkWriteLock(l) && !checkReadLock(client_id, l)) {
            // TODO - What to return if lock is already present for that client_id
            if (!readLockMap.containsKey(l)) {
                readLockMap.put(l, Arrays.asList(client_id));
                return true;
            } else {
                readLockMap.get(l).add(client_id);
            }
            return true;
        }
            
        } catch (RuntimeException e) {
            // Error occured while obtaining lock
            return false;
        }
        return false;
    }

    // Assing the write lock to client_id
    @Override
    public boolean requestWriteLock(int l, int client_id) {
        if(!checkReadLock(l) & !checkWriteLock(l)) {
            writeLockMap.put(l, client_id);
            return true;
        }
        return false;
    }

}
