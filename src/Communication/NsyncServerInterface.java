package Communication;
import java.rmi.*;

import Controller.SendObject;

public interface NsyncServerInterface extends Remote {    
    
    public boolean getPermission(String queueName) throws RemoteException;
    
    public SendObject processSendObject(SendObject s);
    
    public boolean isUp() throws RemoteException;
    
}
