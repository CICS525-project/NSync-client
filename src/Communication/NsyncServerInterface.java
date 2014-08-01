package Communication;

import java.rmi.*;
import Controller.SendObject;
import java.util.Date;

public interface NsyncServerInterface extends Remote {

	public boolean getPermission(String username) throws RemoteException;

	public SendObject serverDBUpdate(SendObject s) throws RemoteException;

	public boolean serverToClientSync(Date lastTS, String qName)
			throws RemoteException;

	public boolean isUp() throws RemoteException;

	public boolean createAccount(String username, String password, String email)
			throws RemoteException;

	public boolean loginUser(String username, String password)
			throws RemoteException;

	public String createQueue(String username) throws RemoteException;

	public boolean verifyUser(String username, String password)
			throws RemoteException;

	public String getGeneratedPassword(String password) throws RemoteException;
}
