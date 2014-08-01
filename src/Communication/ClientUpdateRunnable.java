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
							System.out.println("Just took something from the queue"
									+ QueueManager.convertSendObjectToString(s));
							if (CommunicationManager.server.getPermission(UserProperties
									.getUsername())) {
								SendObject r = CommunicationManager.server
										.processSendObject(s);
								System.out
										.println("R IS ENTEREDTED INTO THE DB"
												+ r.isEnteredIntoDB());
								if (r.isEnteredIntoDB()) {
									String fullPath = UserProperties
											.getDirectory()
											+ r.getFilePath()
											+ r.getFileName();
									System.out.println("\nSend object is "
											+ r.getEvent().toString() + " \n");
									if (r.getEvent().equals(
											SendObject.EventType.Create)
											|| r.getEvent()
													.equals(SendObject.EventType.Modify)) {
										System.out
												.println("\nCalling the upload blob on "
														+ fullPath + " \n");
										BlobManager.uploadFileAsBlob(fullPath);
									} else if (r.getEvent().equals(
											SendObject.EventType.Delete)) {
										System.out
												.println("\nCalling the blob delete on "
														+ fullPath + " \n");
										BlobManager.deleteBlob(fullPath);
									} else if (r.getEvent().equals(
											SendObject.EventType.Rename)) {
										System.out
												.println("\nCalling the blob rename on "
														+ fullPath + " \n");
										BlobManager.renameBlob(
												r.getNewFileName(),
												r.getFileName());
									}
									// Thread.sleep(4000);
									NSyncClient.sentQ.put(r);
								}
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (RemoteException e) {
							NSyncClient.toSendQ.add(s);
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

}
