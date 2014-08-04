package DBManager;

import java.util.concurrent.BlockingQueue;

import Controller.NSyncClient;
import Controller.SendObject;


/**
 * @author yasminf
 *
 */
public class DBSentQManager extends DBManagerLocal implements Runnable 
{
	private static BlockingQueue<SendObject> sentQ;
	
	public DBSentQManager(BlockingQueue<SendObject> sent)
	{
		super();
		//sentQ = sent;
	}

	public void run() 
	{
		SendObject inObj = null;
		while(true)
		{
			try 
			{
				
				inObj = NSyncClient.sentQ.take();
				processQueueSent(inObj);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public static void processQueueSent(SendObject obj) // process queue in events queue class
	{
		String file_id="";
		String new_status="";
		int success=-1;
		String file_path = obj.getFilePath();
		String file_name = obj.getFileName();
		String event = obj.getEvent().toString();
		java.sql.Timestamp last_local_update =  getTimeStamp(obj.getTimeStamp());

		file_id = obj.getID();
		
		System.out.println("file id_________________________________" +file_id);
		System.out.println("file_path_________________________________" +file_path);
		System.out.println("file_name_________________________________" +file_name);
		System.out.println("ROW ID is _________________________________" +file_id);
		
		//new_status = setNewState(event, getCurrentState(file_id));
		System.out.println("NEW STATUS _________________________________" +new_status);
		
		success = localModifyLastServerUpdate(file_id, last_local_update);
		
		System.out.println("Last server and state updated time_stamp updated");
		

	}
}