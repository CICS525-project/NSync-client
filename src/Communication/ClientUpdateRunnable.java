package Communication;

import java.awt.TrayIcon;
import java.rmi.RemoteException;

import Controller.LeaseParams;
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
						LeaseParams lp = null;
						try {
							s = NSyncClient.toSendQ.take();
							System.out.println("Just took something from the queue "
									+ QueueManager.convertSendObjectToString(s));
							int tries = 0;
							while (true) {

								try {
									lp = CommunicationManager.server
											.getPermission(s);
									System.out.println("Lease is "
											+ lp.isLeaseGranted()
											+ lp.getServer1Lease());
									if (lp.isLeaseGranted()) {
										// tries = 0;
										break;
									} else {
										tries++;
										if (tries == 5) {
											TrayIconBasic
													.displayMessage(
															"Error",
															"Someone else is using the resource you want to use. Please try again later. Your changes where not saved to the server",
															TrayIcon.MessageType.ERROR);
											tries = 0;
											break;
										}
										continue;
									}

								} catch (Exception e) {
									System.out
											.println("Permission not granted");
									e.printStackTrace();
									Thread.sleep(5000);
									continue;

								}
							}

							// if (r.isEnteredIntoDB()) {
							String fullPath = UserProperties.getDirectory()
									+ pathParser(s.getFilePath())
									+ s.getFileName();
							System.out.println("\nSend object is "
									+ s.getEvent().toString() + " \n");
							if (s.getEvent()
									.equals(SendObject.EventType.Create)
									|| s.getEvent().equals(
											SendObject.EventType.Modify)) {
								System.out
										.println("\nCalling the upload blob on "
												+ fullPath + " \n");
								BlobManager.uploadFileAsBlob(fullPath,
										getLeaseID(lp));
							} else if (s.getEvent().equals(
									SendObject.EventType.Delete)) {
								System.out
										.println("\nCalling the blob delete on "
												+ fullPath + " \n");
								// BlobManager.deleteBlob(fullPath);
							} else if (s.getEvent().equals(
									SendObject.EventType.Rename)) {
								System.out
										.println("\nCalling the blob rename on "
												+ fullPath + " \n");
								// BlobManager.renameBlob(
							}
							CommunicationManager.server.serverDBUpdate(s,
									UserProperties.getQueueName(), lp);
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

	private static String getLeaseID(LeaseParams p) {
		System.out.println("CommunicationManager: The server connected to is "
				+ p.serverId);
		if (p.serverId == 1) {
			return p.getServer1Lease();
		} else if (p.serverId == 2) {
			return p.getServer2Lease();
		} else {
			return p.getServer3Lease();
		}
	}

}
