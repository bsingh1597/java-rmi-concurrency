package assignment.adcs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;



public class StringArrayClient {

    private int client_id;
    private static RemoteStringArray stringArrayServer;
    Map<Integer, String> localStringArrayMapping;
    String PRINT_STATEMENT = "Select an Option: \n" +
            "1. Get Array Capacity\n" +
            "2. Fetch Element Read\n" +
            "3. Fetch Element Write\n" +
            "4. Print Element \n" +
            "5. Cancatenate Element \n" +
            "6. Writeback Element \n" +
            "7. Release lock \n"+
            "8. Get All Read Lock from Server\n"+
            "9. Get All Write Lock from Server\n" +
            "10. Exit";
    private String SUCCESS = "Success";
    private String FAILURE = "Failure";

    public StringArrayClient(int client_id) {
        this.client_id = client_id;
        localStringArrayMapping = new HashMap<>();
    }

    public int getArrayCapacity() throws RemoteException {
        return stringArrayServer.size();
    }

    public String fetchElementRead(int l) {
        String readStr = null;
        // Check if the element is present the local map
        try {
            readStr = stringArrayServer.fetchElementRead(l, client_id);
            localStringArrayMapping.put(l, readStr);
            return SUCCESS;
        } catch (Exception e) {
            System.err.println("Error while Read fetching: " + e.getMessage());
            return FAILURE;
        }
    }

    public String fetchElementWrite(int l) {
        String writeStr = null;
        try {
            writeStr = stringArrayServer.fetchElementWrite(l, client_id);
            localStringArrayMapping.put(l, writeStr);
            return SUCCESS;
        } catch (Exception e) {
            System.err.println("Error while Write fetching: " + e.getMessage());
            return FAILURE;
        }
        
    }

    // Expected that the element is fetched either in read or write mode
    public String printElement(int l) {
        return localStringArrayMapping.get(l);
    }

    public String concatenate(int l, String concStr) {
        if (localStringArrayMapping.get(l) != null) {
            String newString = localStringArrayMapping.get(l).concat(concStr);
            localStringArrayMapping.put(l, newString);
            return newString;
        } else {
            localStringArrayMapping.put(l, concStr);
            return concStr;
        }
    }

    public String writeBack(int l) throws RemoteException {
        if (stringArrayServer.writeBackElement(localStringArrayMapping.get(l), l, client_id))
            return "Success";
        else
            return "Failure";

    }

    public void releaseLock(int l) throws RemoteException {
        stringArrayServer.releaseLock(l, client_id);
        localStringArrayMapping.remove(l);
    }

    public void releaseLock(){
        localStringArrayMapping.keySet().forEach(l -> {
            try {
                stringArrayServer.releaseLock(l, client_id);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        localStringArrayMapping.clear();
    }

    public String getAllReadLocksAllClients() {
        try {
            return stringArrayServer.getAllReadLocks();
        } catch (RemoteException e) {
           e.printStackTrace();
           return "ERROR";
        }
    }

    public String getAllWriteLocksAllClients() {
        try {
            return stringArrayServer.getAllWriteLocks();
        } catch (RemoteException e) {
           e.printStackTrace();
           return "ERROR";
        }
    }

    private void console() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println(PRINT_STATEMENT);
                Thread.sleep(1000);
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1: {
                        // Get Array Capacity
                        System.out.println("Capacity of Array: " + getArrayCapacity());
                    }
                        break;
                    case 2: {
                        // Fetch Element Read
                        System.out.println("Provide index of the element to fetch: ");
                        int l = scanner.nextInt();
                        // Decrement elementId because the frst element in array is 0
                        l--;
                        System.out.println(fetchElementRead(l));
                    }
                        break;
                    case 3: {
                        // Fetch Element Write
                        System.out.println("Provide index of the element to fetch: ");
                        int l = scanner.nextInt();
                        l--;
                        System.out.println(fetchElementWrite(l));
                    }
                        break;
                    case 4: {
                        // Print Element from the Local Map
                        System.out.println("Provide index of the element to print: ");
                        int l = scanner.nextInt();
                        l--;
                        System.out.println(localStringArrayMapping.get(l));
                        
                    }
                        break;
                    case 5: {
                        // Concatenate Str to l element
                        System.out.println("Provide index of the element to concatenate: ");
                        int l = scanner.nextInt();
                        l--;
                        System.out.println("Enter the string to Concatenate: ");
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        String conStr = in.readLine();
                        System.out.println(concatenate(l, conStr));
                    }
                        break;
                    case 6: {
                        // Write back Element
                        System.out.println("Provide index of the element to write back: ");
                        int l = scanner.nextInt();
                        l--;
                       System.out.println(writeBack(l));
                    }
                        break;
                    case 7: {
                        // Release Lock
                        System.out.println("Provide index of the element to write back: ");
                        int l = scanner.nextInt();
                        l--;
                        releaseLock(l);
                    }
                        break;  
                    case 8: {
                        System.out.println(getAllReadLocksAllClients());
                    }
                        break;
                    case 9: {
                        System.out.println(getAllWriteLocksAllClients());
                    }
                        break;
                    case 10: {
                        releaseLock();
                        System.out.println("All the lock of this Client released");
                        return;
                    }
                    default:
                        System.out.println("Choose correct Option");
                        break;

                }
            } catch (Exception e) {
                // Release all locks
                releaseLock();
                scanner.close();
            }
            System.out.println("***************------------*****************");
        }

    }

    // Local testing without RMI
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(null);
            stringArrayServer = (RemoteStringArray) registry.lookup("RemoteStringArray");
            StringArrayClient client = new StringArrayClient(27);
            client.console();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
      
    }

}
