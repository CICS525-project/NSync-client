package Communication;

import Controller.SendObject;
import Controller.UserProperties;

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
					if (QueueManager.getQueueLength(queue) > 0) {
						// call to DBManager method to resolve conflicts missing
						String message = QueueManager.deque(queue);
						System.out
								.println("The message dequeued is " + message);
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
	
	private static void processMessageFromQueue(SendObject s) {
		if (s.getEvent().equals(SendObject.EventType.Create)
				|| s.getEvent().equals(SendObject.EventType.Modify)) {
			LocalFileManager.download(s);
		}

		if (s.getEvent().equals(SendObject.EventType.Delete)) {
			LocalFileManager.delete(s);
		}

		if (s.getEvent().equals(SendObject.EventType.Rename)) {
			LocalFileManager.rename(s);
		}

	}
}
