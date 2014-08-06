package Communication;

import java.awt.TrayIcon;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import Controller.SendObject;
import Controller.UserProperties;
import DBManager.DBManagerLocal;
import DBManager.DBSyncManager;
import GUI.TrayIconBasic;

public class ConnectClientServer {

	public static Thread pollThread;

	public static void processUpdateFromServer() {
		// get the queue of the client
		System.out.println("The name of the queue is "
				+ UserProperties.getQueueName());
		final String queue = UserProperties.getQueueName();
		pollThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (!CommunicationManager.server.maintainQueue(queue)) {
							// run sync method
							CommunicationManager.verifyUser(
									UserProperties.getUsername(),
									UserProperties.getPassword(),
									UserProperties.getQueueName());
							CommunicationManager.server.serverToClientSync(
									DBManagerLocal.getLastTimeStamp(),
									UserProperties.getQueueName());
						}
					} catch (RemoteException e1) {
						// could be because server is down or because the client
						// was disconnected from internet

						// check if there is internet connection, if there is
						// call the method to find another server
						try {
							if (InetAddress.getByName("google.com")
									.isReachable(3000)) {
								CommunicationManager.connectToServer();
								CommunicationManager.server.serverToClientSync(
										DBManagerLocal.getLastTimeStamp(),
										UserProperties.getQueueName());
							} else {
								// run offline
							}
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

						e1.printStackTrace();
					}
					System.out.println("\n checking the queue length if it is >0. " + queue+ " lengthe: "
                                        + QueueManager.getQueueLength(queue));
                                        if (QueueManager.getQueueLength(queue) > 0) {
						// call to DBManager method to resolve conflicts missing

						String message = QueueManager.deque(queue);
						System.out
								.println("The message dequeued is " + message);
						SendObject d = QueueManager
								.convertStringToSendObject(message);

						if (DBManagerLocal.findConflict(d)) {
							try {
								LocalFileManager.copyConflictedFile(
										d,
										d.getUserID() + "_conflict_"
												+ d.getFileName());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							LocalFileManager.download(d);
							TrayIconBasic.displayMessage(
									"Conflict found on " + d.getFilePath()
											+ "/" + d.getFileName(),
									"New file called "
											+ d.getUserID()
											+ "_conflict_"
											+ d.getFileName()
											+ " has been created in the location of the conflict",
									TrayIcon.MessageType.WARNING);
							
						} 
							DBSyncManager.processObjFromServer(d);
                                                        System.out.println("||||||||| "+ d.getEvent().toString());
							processMessageFromQueue(d);
						
						// call to dbManager to update the SendObject missing
					}

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		// continually check to see if the queue has something in a thread
		pollThread.start();
	}

	private static void processMessageFromQueue(SendObject s) {
		if (s.getEvent().equals(SendObject.EventType.Create)
				|| s.getEvent().equals(SendObject.EventType.Modify)) {
			LocalFileManager.download(s);
			TrayIconBasic.displayMessage("File Added/Updated", s.getFilePath()
					+ "/" + s.getFileName() + " added",
					TrayIcon.MessageType.INFO);
		}

		if (s.getEvent().equals(SendObject.EventType.Delete)) {
			LocalFileManager.delete(s);
			TrayIconBasic.displayMessage("File Deleted", s.getFilePath() + "/"
					+ s.getFileName() + " deleted", TrayIcon.MessageType.INFO);
		}

		if (s.getEvent().equals(SendObject.EventType.Rename)) {
			LocalFileManager.rename(s);
			TrayIconBasic.displayMessage("File Renamed", s.getFilePath() + "/"
					+ s.getFileName() + " renamed", TrayIcon.MessageType.INFO);
		}

	}
}
