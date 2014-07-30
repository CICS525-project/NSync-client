package Controller;

import DBManager.DBController;
import FolderWatcher.FolderWatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NSyncClient {
	public static BlockingQueue<SendObject> toSendQ;
	public static BlockingQueue<SendObject> eventsQ;
	public static BlockingQueue<SendObject> sentQ;
	public static final Path dir = Paths.get(System.getProperty("user.home")
			+ "\\NSync"); // in your user directory, NSYNC is created

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// creating the queues:
		toSendQ = new LinkedBlockingQueue<SendObject>();
		sentQ = new LinkedBlockingQueue<SendObject>();
		eventsQ = new LinkedBlockingQueue<SendObject>();
		/* static variables to initialize folderwatcher and dbcontroller
		public static DBController dbc;
	    public static FolderWatcher fw;
		*/
		// creating new folderWatcher, DBManager, and Communication classes
		try {
			Thread t = new Thread(new DBController(eventsQ, sentQ, toSendQ));
			t.start();
			ClientHelper.initializeClient();
			new FolderWatcher();
			/* new initialization for dbmanager and folder watcher folder watcher needs to run as a thread
			fw = new FolderWatcher();
            new Thread(fw).start();
            dbc = new DBController(eventsQ, toSendQ, sentQ);
            new Thread(dbc).start();
            */
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
