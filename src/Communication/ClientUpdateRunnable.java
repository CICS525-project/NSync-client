package Communication;

import java.awt.TrayIcon;
import java.rmi.RemoteException;

import Controller.NSyncClient;
import Controller.SendObject;
import Controller.UserProperties;
import GUI.TrayIconBasic;

public class ClientUpdateRunnable {
	public static Thread pushThread;

	public static void checkToSendQ() {
		// get the queue of the client
		pushThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					if (CommunicationManager.connectToServer()) {
						System.out.println("Server is up");
						SendObject s = null;
						try {
							s = NSyncClient.toSendQ.take();
							System.out.println("Just took something from the queue "
									+ QueueManager.convertSendObjectToString(s));
							while (true) {
								try {
									CommunicationManager.server
											.getPermission(s);
									break;
								} catch (Exception e) {
									System.out
											.println("Permission not granted");
									Thread.sleep(5000);
									continue;

								}
							}
							SendObject r = CommunicationManager.server
									.serverDBUpdate(s,
											UserProperties.getQueueName());
							// if (r.isEnteredIntoDB()) {
							String fullPath = UserProperties.getDirectory()
									+ pathParser(r.getFilePath())
									+ r.getFileName();
							System.out.println("\nSend object is "
									+ r.getEvent().toString() + " \n");
							if (r.getEvent()
									.equals(SendObject.EventType.Create)
									|| r.getEvent().equals(
											SendObject.EventType.Modify)) {
								System.out
										.println("\nCalling the upload blob on "
												+ fullPath + " \n");
								BlobManager.uploadFileAsBlob(fullPath);
							} else if (r.getEvent().equals(
									SendObject.EventType.Delete)) {
								System.out
										.println("\nCalling the blob delete on "
												+ fullPath + " \n");
								// BlobManager.deleteBlob(fullPath);
							} else if (r.getEvent().equals(
									SendObject.EventType.Rename)) {
								System.out
										.println("\nCalling the blob rename on "
												+ fullPath + " \n");
								// BlobManager.renameBlob(
							}
							NSyncClient.sentQ.put(s);

						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						TrayIconBasic
								.displayMessage(
										"Server Offline",
										"The server is offline so all changes would be stored locally",
										TrayIcon.MessageType.WARNING);
					}
				}
			}
		});
		// continually check to see if the queue has something in a thread
		pushThread.start();
	}

	private static String pathParser(String path) {
		if (path == null || path.equals("")) {
			return "";
		} else {
			return path + "/";
		}
	}

}
