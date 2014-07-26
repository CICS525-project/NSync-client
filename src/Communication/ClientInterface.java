package Communication;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author welcome
 */
public interface ClientInterface extends Remote {
	//public String name;
	
    public double findScore() throws RemoteException;
    
}
