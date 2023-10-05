package assignment.adcs;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
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
    public int size() {
        return strArray.length;
    }

    @Override
    public boolean writeBackElement(String str, int l, int client_id) {

        if(checkWriteLock(client_id, l)) {
            strArray[l] = str;
            return true;
        }
        return false;
    }

    @Override
    public String fetchElementRead(int l, int client_id) {
        // If read lock is not present add lock if possible
        // Write lock can read elements
        if (requestReadLock(l, client_id) || checkWriteLock(client_id, l)) {
            return strArray[l];
        }
        // } else {
        //     throw new RuntimeException("No read Only Lock");
        // }
        return null;
    }

    // Check Read Lock on element for client_id
    // return true if lock is there
    // If write lock is there for the client return true
    private boolean checkReadLock(int client_id, int l) {

        List<Integer> clientList;
        clientList = readLockMap.get(l);
        if(checkWriteLock(client_id, l)) return true;
        if (clientList != null) {
            return clientList.contains(client_id);
        }
        return false;
    }

    // Check Read Lock on element
    // return true if lock is there
    private boolean checkReadLock(int element) {
        return readLockMap.get(element) != null && !readLockMap.get(element).isEmpty();
    }

    // Check Read Lock on element
    // return true if lock is there
    private boolean checkWriteLock(int element) {
        return writeLockMap.get(element) != null;
    }

    private boolean checkWriteLock(int client_id, int element) {
        return checkWriteLock(element) && client_id == writeLockMap.get(element);
    }

     public boolean checkReadLockPerClient(int client_id, int element) {
        if(checkReadLock(element)) {
            return readLockMap.get(element).stream().allMatch(client -> client == client_id);
        }
        return false;

    }

    @Override
    public String fetchElementWrite(int l, int client_id) {
        if(requestWriteLock(l, client_id)) {
            return strArray[l];
        }
        return null;
    }

    @Override
    public void insertArrayElement(int l, String str) {
        if (l >= 0 && l < strArray.length) {
            strArray[l] = str;
        }
    }

    // Release any read or write lock on the element for that Id
    // Only client having lock will call it
    @Override
    public void releaseLock(int l, int client_id) {

        try {
            if (writeLockMap.get(l) != l && writeLockMap.get(l) == client_id) {
                writeLockMap.remove(l);
            }
            if (readLockMap.get(l) != null) {
                readLockMap.get(l).remove(client_id);
            }
        } catch (RuntimeException e) {

        }

    }

    @Override
    public void releaseReadLock(int l, int client_id) {

        try {
            if (readLockMap.get(l) != null) {
                readLockMap.get(l).remove(client_id);
            }
        } catch (RuntimeException e) {

        }

    }

    @Override
    public boolean requestReadLock(int l, int client_id) {
        try {
            // If write lock is there then no read lock
            if (checkReadLock(client_id, l)) {
                return true;
            } else if (!checkWriteLock(l)) {
                if (!readLockMap.containsKey(l)) {
                    readLockMap.put(l, Arrays.asList(client_id));
                    return true;
                } else {
                    readLockMap.get(l).add(client_id);
                }
                return true;
            }

        } catch (RuntimeException e) {
            System.out.println("Error occured while obtaining read lock for Client: " + client_id +
                    " elementId: " + l + "Error: " + e.getMessage());
        }
        return false;
    }

    // Assing the write lock to client_id
    @Override
    public boolean requestWriteLock(int l, int client_id) {
        if (checkWriteLock(client_id, l))
            return true;
        // Release readLock by this client on that element. If there
        releaseReadLock(l, client_id);
        if (!checkReadLock(l) & !checkWriteLock(l)) {
            writeLockMap.put(l, client_id);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            RemoteStringArrayServer server = new RemoteStringArrayServer(10);
            server.insertArrayElement(0, "bipul");
            server.insertArrayElement(4, "singh");
            RemoteStringArray stub = (RemoteStringArray) UnicastRemoteObject.exportObject(server, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("RemoteStringArray", stub);
            System.out.println("Server has Started....");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
