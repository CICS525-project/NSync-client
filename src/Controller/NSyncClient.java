package Controller;

import GUI.ClientHelper;
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
			ClientHelper.initializeClient();			
			
			Thread t = new Thread(new DBController(eventsQ, sentQ, toSendQ));
			t.start();		
			
			Thread tFolderWatcher = new Thread(new FolderWatcher());
                        tFolderWatcher.start();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
