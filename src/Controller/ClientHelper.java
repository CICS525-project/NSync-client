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
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

public class ClientHelper {

	public static Thread pollThread;
	public static Thread pushThread;

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
			pollSendObject();
			pushSendObject();
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
			//syncFilesAtStartUp(null);

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

	/* create a queue for the client. I still we should use a fixed queue */
	private static String createQueue(String username) {
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
				// check if the user signed up first before logging in
				queuename = retrieveQueueName();
				File file = new File(System.getProperty("user.dir")
						+ "/src/Settings/temp.txt");
				if (file.exists()) {
					file.delete();
				}
			} catch (IOException e) {
				// e.printStackTrace();
				// check if the user already has an account but logged in
				// another client
				queuename = createQueue(username);
				UserProperties.setQueueName(queuename);
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

	public static void pollSendObject() {
		// get the queue of the client
		System.out.println("The name of the queue is "
				+ UserProperties.getQueueName());
		final String queue = UserProperties.getQueueName();
		pollThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					if (QueueManager.getQueueLength(queue) > 0) {
						// call to DBManager method to resolve conflicts missing
						String message = QueueManager.deque(queue);
						System.out.println("The message dequeued is " + message);
						SendObject d = QueueManager
								.convertStringToSendObject(message);
						processMessageFromQueue(d);
						// call to dbManager to update the SendObject missing
					}

					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		// continually check to see if the queue has something in a thread
		pollThread.start();
	}

	public static void processMessageFromQueue(SendObject s) {
		if (s.getEvent().equals(SendObject.EventType.Create)
				|| s.getEvent().equals(SendObject.EventType.Modify)) {
			downloadFile(s.getFilePath() + "/" + s.getFileName());
		}

		if (s.getEvent().equals(SendObject.EventType.Delete)) {
			deleteLocalFile(s.getFilePath() + "/" + s.getFileName());
		}

		if (s.getEvent().equals(SendObject.EventType.Rename)) {
			renameLocalFile(s.getFilePath() + "/" + s.getFileName(),
					s.getNewFileName());
		}

	}

	private static void downloadFile(String filePath) {
		BlobManager.downloadBlob(filePath);
	}

	private static void deleteLocalFile(String filePath) {
		File f = new File(filePath);
		if (f.exists()) {
			f.delete();
		}
	}

	private static void renameLocalFile(String filePath, String newName) {
		String filename, path;
		if (filePath.contains("\\")) {
			filePath.replaceAll("\\", "/");
		}

		if (filePath.contains("/")) {
			filename = filePath.substring(filePath.lastIndexOf("/"));
			path = filePath.substring(0, filePath.lastIndexOf("/"));
			File oldname = new File(UserProperties.getDirectory() + path
					+ filename);
			File newname = new File(UserProperties.getDirectory() + path
					+ newName);
			oldname.renameTo(newname);
		} else {
			filename = filePath;
			File oldname = new File(UserProperties.getDirectory() + filename);
			File newname = new File(UserProperties.getDirectory() + newName);
			oldname.renameTo(newname);
		}
	}

	public static void pushSendObject() {
		// get the queue of the client
		pushThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					if (Connection.isServerUp()) {
						System.out.println("Server is up");
						SendObject s = null;
						try {
							s = NSyncClient.toSendQ.take();
							System.out.println("Just took something from the queue" + QueueManager.convertSendObjectToString(s));
							if (Connection.server.getPermission(UserProperties.getUsername())) {
								SendObject r = Connection.server
										.processSendObject(s);
								System.out.println("R IS ENTEREDTED INTO THE DB" + r.isEnteredIntoDB());
								if (r.isEnteredIntoDB()) {
									String fullPath = UserProperties
											.getDirectory()
											+ r.getFilePath()
											+ r.getFileName();
									// BlobManager
									if (r.getEvent().equals(
											SendObject.EventType.Create)
											|| r.getEvent()
													.equals(SendObject.EventType.Modify)) {
										BlobManager.uploadFileAsBlob(fullPath);
									} else if (r.getEvent().equals(
											SendObject.EventType.Delete)) {
										BlobManager.deleteBlob(fullPath);
									} else if (r.getEvent().equals(
											SendObject.EventType.Rename)) {
										BlobManager.renameBlob(
												r.getNewFileName(),
												r.getFileName());
									}
									NSyncClient.sentQ.add(r);
								}
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (RemoteException e) {
							NSyncClient.toSendQ.add(s);
							e.printStackTrace();
						}
					}
				}
			}
		});
		// continually check to see if the queue has something in a thread
		pushThread.start();
	}
}
