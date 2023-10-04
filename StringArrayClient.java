import java.util.HashMap;
import java.util.Map;

public class StringArrayClient {

    private int client_id;
    private RemoteStringArray stringArrayServer;
    String currentWriteStr;
    Map<Integer, String> localStringArrayMapping;

    public StringArrayClient(int client_id) {
        this.client_id = client_id;
        localStringArrayMapping = new HashMap<>();
    }

    public int getArrayCapacity() {
        return stringArrayServer.size();
    }

    public String fetchElementRead(int l) {
        String readStr = null;
        try {
             readStr =  stringArrayServer.fetchElementRead(l, client_id);
             if(readStr != null) {
                localStringArrayMapping.put(l, readStr);
             }
        } catch (Exception e) {
            System.err.println("Error while Read fetching: "+e.getMessage());
        }
        return readStr;
        
    }

    public String fetchElementWrite(int l) {
        String writeStr = null;
        try {
            writeStr = stringArrayServer.fetchElementWrite(l, client_id);
        } catch (Exception e) {
            System.err.println("Error while Write fetching: "+e.getMessage());
        }
        return writeStr;
    }

    // Expected that the element is fetched either in read or write mode
    public String printElement(int l) {
        return localStringArrayMapping.get(l);
    }

    public boolean concatenate(int l, String concStr){
        if(localStringArrayMapping.get(l) != null) {
            localStringArrayMapping.put(l, localStringArrayMapping.get(l).concat(concStr));
            return true;
        }
        return false;
    }

    public String writeBack(int l) {
        if(stringArrayServer.writeBackElement(localStringArrayMapping.get(l), l, client_id))
            return "Success";
        else
            return "Failure";
        
    }


    
}
