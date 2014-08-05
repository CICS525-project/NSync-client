package Communication;
import Controller.LeaseParams;
import Controller.SendObject;
import java.rmi.*;

import java.sql.Timestamp;

public interface NsyncServerInterface extends Remote {      
	 
    public LeaseParams getPermission(SendObject s) throws RemoteException;
    
    public boolean getPermissionForServers(String username) throws RemoteException;
    
    public SendObject serverDBUpdate(SendObject s, String queuename, LeaseParams p ) throws RemoteException;
    
    public void serverToClientSync(Timestamp lastTS, String qName) throws RemoteException;
    
    public boolean isUp() throws RemoteException;
    
    public boolean createAccount(String username, String password, String email) throws RemoteException;
    
    public boolean loginUser(String username, String password) throws RemoteException;
    
    public String createQueue(String username, String oldQueueName) throws RemoteException;  
    
    public boolean maintainQueue(String queuename) throws RemoteException;
    
    public boolean verifyUser(String username, String password) throws RemoteException;
    
    public String getGeneratedPassword(String password) throws RemoteException;
}

