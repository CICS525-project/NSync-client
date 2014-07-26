package Controller;

import BlobManager.BlobManager;
import Communication.Connection;
import Communication.QueueManager;

import java.awt.EventQueue;
import java.awt.TrayIcon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
			performSync();
		}

	}
	
	public static void performSync() {
		Map<String, String> userParams = null;
		try {
			userParams = loggedInBefore();
		} catch (IOException e) {
			e.printStackTrace();
		}
		UserProperties.setQueueName(userParams.get("queuename"));
		UserProperties.setUsername(userParams.get("username"));
		UserProperties.setPassword(userParams.get("password"));

		System.out.println(userParams.get("queuename"));

		// if the client can connect to a server do other things
		if (Connection.isServerUp()) {
			// sync files
			syncFilesAtStartUp(null);

			// continuallySyncFiles(null);
		} else {
			TrayIconBasic
					.displayMessage(
							"ERROR",
							"Connection to server unavailable. All changes would be stored locally",
							TrayIcon.MessageType.ERROR);
		}
	}

	public static UserProperties performAuthentication() {
		// check if this has been done before if not ask user for credentials

		// if new user create container
		return null;
	}

	public static int connectToServer(UserProperties u) {

		return 0;
	}

	public static void syncFilesAtStartUp(UserProperties u) {
		// run the file syncing in a thread

		Thread initClient = new Thread(new Runnable() {
			public void run() {
				BlobManager.setContainerName(UserProperties.getUsername());
				BlobManager.downloadAllBlobs();
			}
		});
		initClient.start();
	}

	public static void continuallySyncFiles(UserProperties u) {
		Thread initClient = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(600000);
				} catch (InterruptedException ex) {
					Logger.getLogger(ClientHelper.class.getName()).log(
							Level.SEVERE, null, ex);
				}
				while (true) {
					BlobManager.downloadAllBlobs();
					try {
						System.out.println("Continually sync thread started");
						Thread.sleep(600000);
					} catch (InterruptedException ex) {
						Logger.getLogger(ClientHelper.class.getName()).log(
								Level.SEVERE, null, ex);
					}
				}
			}
		});
		initClient.start();
		System.out.println("Continually sync thread started");
	}

	public static void maintainConnection() {
		// constantly tell the server you are around
		new Runnable() {
			public void run() {
				// add the socket code to communicate with server here

				Process p1 = null;
				try {
					p1 = java.lang.Runtime.getRuntime().exec(
							"ping www.google.com");
					System.out.println(p1.waitFor());
				} catch (IOException ex) {
					Logger.getLogger(ClientHelper.class.getName()).log(
							Level.SEVERE, null, ex);
				} catch (InterruptedException ex) {
					Logger.getLogger(ClientHelper.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		};
	}

	/* create a queue for the client. I still we should use a fixed queue */
	private static String createQueue(String username) {
		// UserProperties.setQueueName(User.getUsername() + new
		// Date().getTime());
		String queuename = username + new Date().getTime();
		QueueManager.createQueue(queuename);
		return queuename;
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
		if (dbParams.size() > 0)
			return dbParams;
		else
			return null;
	}

	public static void main(String[] args) {
		try {
			Map<String, String> params = ClientHelper.loggedInBefore();
			System.out.println(params.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public static boolean loginUser(ClientGUI cg, String username,
			String password) {
		// call db method to login
		// the line below is just for demo purposes
		String queuename = null;

		if (username.equals("democontainer")
				&& password.equals("democontainer")) {

			try {
				//check if the user signed up first before logging in
				queuename = retrieveQueueName();
				File file = new File(System.getProperty("user.dir")
						+ "/src/Settings/temp.txt");
				if (file.exists()) {
					file.delete();
				}
			} catch (IOException e) {
				//e.printStackTrace();
				//check if the user already has an account but logged in another client
				queuename = createQueue(username);
			}

			writeUserParamsToFile(username, password, queuename);
			TrayIconBasic.displayMessage("Alert", "Login Successful",
					TrayIcon.MessageType.INFO);
			cg.dispose();
			// delete the temp file
			performSync();
			return true;
		}

		cg.getMessage().setText("Login failed. Please try again");
		return false;
	}

	public static boolean createAccount(ClientSignUpGUI jd, String username,
			String password, String email) {
		try {
			// call database method to add account

			// create a container for the user
			BlobManager.createContainter(username);

			// create a queue for the user using the username
			String queuename = createQueue(username);
			// if account creation is successful, tell the user and write to the
			// settings file
			jd.setMessage("Account successfully created");

			storeQueueNameAfterSignUp(queuename);

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static void storeQueueNameAfterSignUp(String queuename) {
		try {
			File file = new File(System.getProperty("user.dir")
					+ "/src/Settings/temp.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(queuename);
			bw.close();
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String retrieveQueueName() throws IOException {
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(
				System.getProperty("user.dir") + "/src/Settings/temp.txt"));

		String line = reader.readLine();
		reader.close();
		return line.trim();
	}

	private static void writeUserParamsToFile(String username, String password,
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
