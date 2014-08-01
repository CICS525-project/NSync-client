package Communication;

import Communication.NsyncServerInterface;
import Controller.UserProperties;
import GUI.ClientGUI;
import GUI.ClientHelper;
import GUI.ClientSignUpGUI;
import GUI.TrayIconBasic;

import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.rmi.ssl.SslRMIClientSocketFactory;

public class CommunicationManager {

	public static boolean watchFolder = true;

	public static int serverPort = 9005;

	public static int serverId = getRandomServer();

	public static NsyncServerInterface server;

	private static int getRandomServer() {
		Random rand = new Random();
		int n = rand.nextInt(3) + 1;
		return n;
	}

	public static ArrayList<Integer> getOtherServerIds() {
		ArrayList<Integer> serverIds = new ArrayList<Integer>();
		for (int i = 1; i <= 3; i++) {
			if (i != serverId)
				serverIds.add(i);
		}
		return serverIds;
	}

	public static Map<String, String> getServerConnectionParams(int serverId) {
		Map<String, String> connParams = new HashMap<String, String>();
		if (serverId == 1) {
			String storageConnectionString = "DefaultEndpointsProtocol=http;"
					+ "AccountName=portalvhdsh8ghz0s9b7mx9;"
					+ "AccountKey=ThVIUXcwpsYqcx08mtIhRf6+XxvEuimK35/M65X+XlkdVCQNl4ViUiB/+tz/nq+eeZAEZCNrmFVQwwN3QiykvA==";
			String dbConnectionString = "jdbc:sqlserver://jvaakzlcvo.database.windows.net:1433"
					+ ";"
					+ "database=db_like"
					+ ";"
					+ "user=yanki@jvaakzlcvo" + ";" + "password=almeta%6y";
			connParams.put("storageConnectionString", storageConnectionString);
			connParams.put("dbConnectionString", dbConnectionString);
			connParams.put("serverIP", "138.91.113.97");
			connParams.put("url",
					"https://portalvhdsh8ghz0s9b7mx9.blob.core.windows.net/");
		}

		if (serverId == 2) {
			
                        String storageConnectionString = "DefaultEndpointsProtocol=http;"
					+ "AccountName=portalvhds27bmmb28df76b;"
					+ "AccountKey=+/R+aDSrb9BDH+HuU2eciV2fP4l6C1timdwao5czZrLE2vML3lt0omtSJhsL4NNi7rmTDWEflNlYXaeh0k+IZw==";
			String dbConnectionString = "jdbc:sqlserver://e55t52o9fy.database.windows.net:1433"
					+ ";"
					+ "database=db_like"
					+ ";"
					+ "user=db2@e55t52o9fy" + ";" + "password=NSyncgroup5";
			connParams.put("storageConnectionString", storageConnectionString);
			connParams.put("dbConnectionString", dbConnectionString);
			connParams.put("serverIP", "137.135.56.127");
			connParams.put("url",
					"https://portalvhds27bmmb28df76b.blob.core.windows.net/");
                        
                        
                        
		}

		if (serverId == 3) {
			String storageConnectionString = "DefaultEndpointsProtocol=http;"
					+ "AccountName=portalvhdsh8ghz0s9b7mx9;"
					+ "AccountKey=ThVIUXcwpsYqcx08mtIhRf6+XxvEuimK35/M65X+XlkdVCQNl4ViUiB/+tz/nq+eeZAEZCNrmFVQwwN3QiykvA==";
			String dbConnectionString = "jdbc:sqlserver://ah0sncq8yf.database.windows.net:1433"
					+ ";"
					+ "database=db_like"
					+ ";"
					+ "user=MySQLAdmin@ah0sncq8yf" + ";" + "password=almeta%6y";
			connParams.put("storageConnectionString", storageConnectionString);
			connParams.put("dbConnectionString", dbConnectionString);
			connParams.put("serverIP", "138.91.113.97");
			connParams.put("url",
					"https://portalvhdsh8ghz0s9b7mx9.blob.core.windows.net/");
		}
		return connParams;
	}

	public static String getStorageConnectionString() {
		Map<String, String> connParams = getServerConnectionParams(serverId);
		return connParams.get("storageConnectionString");
	}

	public static String getDBConnectionString() {
		Map<String, String> connParams = getServerConnectionParams(serverId);
		return connParams.get("dbConnectionString");
	}

	public static String getURL() {
		Map<String, String> connParams = getServerConnectionParams(serverId);
		System.out.println("the url is " + connParams.get("url"));
		return connParams.get("url");
	}

	public static boolean connectToServer() {
		Map<String, String> connParams = getServerConnectionParams(serverId);
		System.out.println(connParams.get("serverIP"));
		System.setProperty("javax.net.ssl.keyStore",
				System.getProperty("user.dir")
						+ "\\src\\Settings\\clientkeystore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "justdoit525");
		System.setProperty("javax.net.ssl.trustStore",
				System.getProperty("user.dir")
						+ "\\src\\Settings\\clienttruststore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "justdoit525");
		System.setProperty("javax.net.ssl.debug", "all");

		try {
			System.setProperty("java.rmi.server.hostname",
					connParams.get("serverIP"));
			Registry registry = LocateRegistry.getRegistry(
					connParams.get("serverIP"), serverPort,
					new SslRMIClientSocketFactory());
			server = (NsyncServerInterface) registry
					.lookup("ServerInterfaceImpl");
			server.isUp();
			return true;
		} catch (NotBoundException | RemoteException e) {
			e.printStackTrace();
			serverId = getOtherServerIds().get(0);
			connParams = getServerConnectionParams(serverId);
			try {
				System.setProperty("java.rmi.server.hostname",
						connParams.get("serverIP"));
				Registry registry = LocateRegistry.getRegistry(
						connParams.get("serverIP"), serverPort,
						new SslRMIClientSocketFactory());
				server = (NsyncServerInterface) registry
						.lookup("ServerInterfaceImpl");
				server.isUp();
				return true;
			} catch (NotBoundException | RemoteException e2) {
				e2.printStackTrace();
				serverId = getOtherServerIds().get(1);
				connParams = getServerConnectionParams(serverId);
				try {
					System.setProperty("java.rmi.server.hostname",
							connParams.get("serverIP"));
					Registry registry = LocateRegistry.getRegistry(
							connParams.get("serverIP"), serverPort,
							new SslRMIClientSocketFactory());
					server = (NsyncServerInterface) registry
							.lookup("ServerInterfaceImpl");
					server.isUp();
					return true;
				} catch (NotBoundException | RemoteException e3) {
					e3.printStackTrace();
					return false;
				}
			}
		}
		// return false;
	}

	public static boolean createAccount(ClientSignUpGUI jd, String username,
			String password, String email) {
		try {
			
			//System.out.println(server.createAccount(username, password, email));
			// call remote method to create account
			if (server.createAccount(username, password, email)) {
				//jd.setMessage("Account successfully created");
				return true;
			} else {
				//jd.setMessage("Account creation failed");
				return false;
			}			
		} catch (Exception e) {
			return false;
		}
		
	}

	public static boolean loginUser(ClientGUI cg, String username,
			String password) {
		// call rmi method to login
		try {
			if (server.loginUser(username, password)) {
				String queuename = null;
				queuename = server.createQueue(username);
				String hash = server.getGeneratedPassword(password);
				ClientHelper.writeUserParamsToFile(username, hash,
						queuename);
				TrayIconBasic.displayMessage("Alert", "Login Successful",
						TrayIcon.MessageType.INFO);
				cg.dispose();
				return true;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cg.getMessage().setText("Login failed. Please try again");
		return false;
	}

	public static boolean verifyUser(String username, String password) {
		// call db method to login
		try {
			if (server.verifyUser(username, password)) {
				String queuename = null;
				queuename = server.createQueue(username);
				ClientHelper.writeUserParamsToFile(username, password, queuename);
				TrayIconBasic.displayMessage("Alert", "Account Verified",
						TrayIcon.MessageType.INFO);
				return true;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TrayIconBasic.displayMessage("Error!!!", "Your account could not be verified. Please login again",
				TrayIcon.MessageType.ERROR);
		return false;
		
	}

}
