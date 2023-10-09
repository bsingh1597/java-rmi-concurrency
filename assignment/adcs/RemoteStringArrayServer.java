package assignment.adcs;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteStringArrayServer implements RemoteStringArray {

    String[] strArray;
    ConcurrentHashMap<Integer, List<Integer>> readLockMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Integer> writeLockMap = new ConcurrentHashMap<>();

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
    public String fetchElementRead(int l, int client_id) throws RemoteException {
        // If read lock is not present add lock if possible
        // Write lock can read elements
        if (requestReadLock(l, client_id) || checkWriteLock(client_id, l)) {
            return strArray[l];
        } else {
             throw new RemoteException("Read Lock Not Obtained");
        }
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
    public String fetchElementWrite(int l, int client_id) throws RemoteException {
        if(requestWriteLock(l, client_id)) {
            return strArray[l];
        } else {
            throw new RemoteException("Write Lock not Obtained");
        }
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
    public boolean releaseLock(int l, int client_id) {
	System.out.println("Release lock for client " + client_id);
        
        try {
            if (writeLockMap.get(l) != null && writeLockMap.get(l) == client_id) {
                writeLockMap.remove(l);
		System.out.println("In release writelock");
            } 
            if (readLockMap.get(l) != null && !readLockMap.get(l).isEmpty()) {
                List<Integer> clientList = readLockMap.get(l);
                clientList.remove(Integer.valueOf(client_id));
                readLockMap.put(l, clientList);
                System.out.println(readLockMap.get(l));
            } 
	    return true;
        } catch (RuntimeException e) {
		e.printStackTrace();
        }
        return false;
    }

    @Override
    public void releaseReadLock(int l, int client_id) {

        try {
            if (readLockMap.get(l) != null) {
                List<Integer> clientList = readLockMap.get(l);
                clientList.remove(client_id);
                readLockMap.put(l, clientList);
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
                    readLockMap.put(l, new ArrayList<>(Arrays.asList(client_id)));
                    return true;
                } else {
                    // readLockMap.get(l).add(client_id);
                    List<Integer> clientList = readLockMap.get(l);
                    clientList.add(client_id);
                    readLockMap.put(l, clientList);
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
        releaseReadLock(l, client_id); //not needed to release read lock once u provide writelock to the same client
        if (!checkReadLock(l) && !checkWriteLock(l)) {
            writeLockMap.put(l, client_id);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            // Bind name (the name associated with the remote object)
            // Capacity of the array
            // List of strings needed to initialize the array
            // Any other configuration parameters your implementation needs such as
            // the port number of the registry (if not using the standard registry
            // port)
            List<String> configFile = Files.readAllLines(Paths.get(args[0]));
            String host = configFile.get(3);
            int port = 9100;
            String bindName = configFile.get(0);
            int capacity = Integer.parseInt(configFile.get(1));
            String[] arrayElements = configFile.get(2).split(" ");
            RemoteStringArrayServer server = new RemoteStringArrayServer(capacity);
            
            for(int i = 0; i<arrayElements.length; i++)
                server.insertArrayElement(i, arrayElements[i]);

            RemoteStringArray stub = (RemoteStringArray) UnicastRemoteObject.exportObject(server, 0);

            // Bind the remote object's stub in the registry
            System.setProperty("java.rmi.server.hostname", host);
            Registry registry = LocateRegistry.getRegistry(host, port);
            registry.rebind(bindName, stub);
            System.out.println("Server has Started....");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Testing
    @Override
    public String getAllReadLocks() throws RemoteException {
        return readLockMap.toString();
    }

    // Testing
    @Override
    public String getAllWriteLocks() throws RemoteException {
        return writeLockMap.toString();
    }

}
