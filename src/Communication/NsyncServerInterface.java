package Communication;
import java.rmi.*;

public interface NsyncServerInterface extends Remote {
    public double findScore(String name) throws RemoteException;    
    
    public String echoIP(ClientInterface client) throws RemoteException;
    
}
