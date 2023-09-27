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
        boolean readLockObtained = checkReadLock(client_id, l);

        if (readLockObtained) {
            return strArray[l];
        } else {
            throw new RuntimeException("No read Only Lock");
        }
    }

    private boolean checkReadLock(int client_id, int l) {

        List<Integer> clientList;
        clientList = readLockMap.get(l);
        if (clientList != null) {
            return clientList.contains(client_id);
        }
        return false;
    }

    private boolean checkWriteLock(int element) {
        if(writeLockMap.get(element) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String fetchElementWrite(int l, int client_id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertArrayElement(int l, String str) {
        strArray[l] = str;
    }

    @Override
    public void releaseLock(int l, int client_id) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean requestReadLock(int l, int client_id) {
        try {
            // If write lock is there then no lock
            if (!checkWriteLock(l) && !checkReadLock(client_id, l)) {
            // TODO - What to return if lock is already present
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

    @Override
    public boolean requestWriteLock(int l, int client_id) {
        // TODO Auto-generated method stub
        return false;
    }

}
