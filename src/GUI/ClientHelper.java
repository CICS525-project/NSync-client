package GUI;

import Communication.ClientUpdateRunnable;
import Communication.ConnectClientServer;
import Communication.CommunicationManager;
import Controller.UserProperties;

import java.awt.EventQueue;
import java.awt.TrayIcon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

public class ClientHelper {

	public static void initializeClient() {
		// create default directory where the program would store info
		File dir = new File(UserProperties.getDirectory());
		if (!dir.exists()) {
			dir.mkdir();
		}

		// start the tray icon
		new TrayIconBasic();
		
		CommunicationManager.connectToServer();

		// check if the user has successfully logged in before
		Map<String, String> userParams = null;
		try {
			userParams = loggedInBefore();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (userParams == null) {
			startClientGUI();
		} else {
			setUserParams();
			ConnectClientServer.processUpdateFromServer();
			ClientUpdateRunnable.checkToSendQ();
		}
	}

	private static void setUserParams() {
		Map<String, String> userParams = null;
		try {
			userParams = loggedInBefore();
		} catch (IOException e) {
			e.printStackTrace();
		}
		UserProperties.setQueueName(userParams.get("queuename"));
		UserProperties.setUsername(userParams.get("username"));
		UserProperties.setUserId(userParams.get("username"));
		UserProperties.setPassword(userParams.get("password"));

		System.out.println(userParams.get("queuename"));
	}

	public static void performSync() {

		// if the client can connect to a server do other things
		if (CommunicationManager.connectToServer()) {
			// do something
		} else {
			TrayIconBasic
					.displayMessage(
							"ERROR",
							"Connection to server unavailable. All changes would be stored locally",
							TrayIcon.MessageType.ERROR);
		}
	}

	/*
	 * used to check if the login frame should be displayed or not. If the map
	 * size is greater than zero then it means that the client has logged in
	 * before
	 */
	private static Map<String, String> loggedInBefore() throws IOException {

		Map<String, String> dbParams = new HashMap<String, String>();
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(
				System.getProperty("user.dir") + "/src/Settings/settings.txt"));

		String line;
		String[] l;

		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			if (line.length() > 4) {
				l = line.split("-");
				dbParams.put(l[0].trim(), l[1].trim());
			}
		}
		reader.close();
		if (dbParams.size() > 0) {
			System.out.println(dbParams.get("password"));
			if (CommunicationManager.verifyUser(dbParams.get("username"),
					dbParams.get("password"))) {
				return dbParams;
			} else {
				return null;
			}
		} else
			return null;
	}

	private static void startClientGUI() {

		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI frame = new ClientGUI();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/*
	 * public static void storeQueueNameAfterSignUp(String queuename) { try {
	 * File file = new File(System.getProperty("user.dir") +
	 * "/src/Settings/temp.txt");
	 * 
	 * // if file doesnt exists, then create it if (!file.exists()) {
	 * file.createNewFile(); }
	 * 
	 * FileWriter fw = new FileWriter(file.getAbsoluteFile()); BufferedWriter bw
	 * = new BufferedWriter(fw); bw.write(queuename); bw.close();
	 * System.out.println("Done"); } catch (IOException e) {
	 * e.printStackTrace(); } }
	 * 
	 * public static String retrieveQueueName() throws IOException {
	 * BufferedReader reader = null; reader = new BufferedReader(new FileReader(
	 * System.getProperty("user.dir") + "/src/Settings/temp.txt"));
	 * 
	 * String line = reader.readLine(); reader.close(); return line.trim(); }
	 */

	public static void writeUserParamsToFile(String username, String password,
			String queuename) {
		try {
			String content = "username - " + username + "\n" + "password - "
					+ password + "\n" + "queuename - " + queuename;

			File file = new File(System.getProperty("user.dir")
					+ "/src/Settings/settings.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
