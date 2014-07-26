package Communication;

import java.util.ArrayList;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.queue.*;

public class QueueManager {
	public static final String storageConnectionString = Connection.getStorageConnectionString();

	public static void createQueue(String queueName) {
		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(storageConnectionString);

			// Create the queue client.
			CloudQueueClient queueClient = storageAccount
					.createCloudQueueClient();

			// Retrieve a reference to a queue.
			CloudQueue queue = queueClient.getQueueReference(queueName);

			// Create the queue if it doesn't already exist.
			queue.createIfNotExists();
		} catch (Exception e) {
			// Output the stack trace.
			e.printStackTrace();
		}
	}

	public static void enqueue(String mes, String queueName) {
		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(storageConnectionString);

			// Create the queue client.
			CloudQueueClient queueClient = storageAccount
					.createCloudQueueClient();

			// Retrieve a reference to a queue.
			CloudQueue queue = queueClient.getQueueReference(queueName);

			// Create the queue if it doesn't already exist.
			queue.createIfNotExists();

			// Create a message and add it to the queue.
			CloudQueueMessage message = new CloudQueueMessage(mes);
			queue.addMessage(message);
		} catch (Exception e) {
			// Output the stack trace.
			e.printStackTrace();
		}
	}

	public static void deque(String queueName) {
		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(storageConnectionString);

			// Create the queue client.
			CloudQueueClient queueClient = storageAccount
					.createCloudQueueClient();

			// Retrieve a reference to a queue.
			CloudQueue queue = queueClient.getQueueReference(queueName);

			// Retrieve the first visible message in the queue.
			CloudQueueMessage retrievedMessage = queue.retrieveMessage(200,
					null, null);

			if (retrievedMessage != null) {
				// Process the message in less than 30 seconds, and then delete
				// the message.
				queue.deleteMessage(retrievedMessage);
			}
		} catch (Exception e) {
			// Output the stack trace.
			e.printStackTrace();
		}
	}

	public static void deleteQueue(String queueName) {
		try {
			// Retrieve storage account from connection-string.
			CloudStorageAccount storageAccount = CloudStorageAccount
					.parse(storageConnectionString);

			// Create the queue client.
			CloudQueueClient queueClient = storageAccount
					.createCloudQueueClient();

			// Retrieve a reference to a queue.
			CloudQueue queue = queueClient.getQueueReference(queueName);

			// Delete the queue if it exists.
			queue.deleteIfExists();
		} catch (Exception e) {
			// Output the stack trace.
			e.printStackTrace();
		}
	}
	
	public static ArrayList<CloudQueue> getListOfQueues(String prefix) {
		ArrayList<CloudQueue> queues = new ArrayList<CloudQueue>();
		try
		{
		    // Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount =
		        CloudStorageAccount.parse(storageConnectionString);

		    // Create the queue client.
		    CloudQueueClient queueClient =
		        storageAccount.createCloudQueueClient();

		    // Loop through the collection of queues.
		    for (CloudQueue queue : queueClient.listQueues(prefix))
		    {
		        // Output each queue name.
		        System.out.println(queue.getName());
		        queues.add(queue);
		    }
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
		return queues;
	}
	
	public static long getQueueLength(String queueName) {		
		long cachedMessageCount = 0;
		try
		{
		    // Retrieve storage account from connection-string.
		    CloudStorageAccount storageAccount = 
		       CloudStorageAccount.parse(storageConnectionString);

		    // Create the queue client.
		    CloudQueueClient queueClient = storageAccount.createCloudQueueClient();

		    // Retrieve a reference to a queue.
		    CloudQueue queue = queueClient.getQueueReference(queueName);

		   // Download the approximate message count from the server.
		    queue.downloadAttributes();

		    // Retrieve the newly cached approximate message count.
		    cachedMessageCount = queue.getApproximateMessageCount();

		    // Display the queue length.
		    System.out.println(String.format("Queue length: %d", cachedMessageCount));
		}
		catch (Exception e)
		{
		    // Output the stack trace.
		    e.printStackTrace();
		}
		return cachedMessageCount;
	}
	
	public static void main(String[] args) {
		//System.out.println(User.getUsername());
		getListOfQueues("democontainer");
		enqueue("Yanki don kolo", "democontainer1406268314962");
		getQueueLength("democontainer1406268314962");
		deque("democontainer1406268314962");
		getQueueLength("democontainer1406268314962");
	}
}
