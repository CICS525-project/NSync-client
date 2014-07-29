package DBManager;

import java.util.concurrent.BlockingQueue;

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
		sentQ = sent;
	}

	public void run() 
	{
		SendObject inObj = null;
		while(true)
		{
			try 
			{
				inObj = sentQ.take();
				processQueue(inObj);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public static void processQueue(SendObject obj) // process queue in events queue class
	{
		String file_id="";
		int success=-1;
		String file_path = obj.getFilePath();
		String file_name = obj.getFileName();
		java.sql.Timestamp last_local_update =  getTimeStamp(obj.getTimeStamp());

		file_id = getrowID(file_path, file_name);
		success = localModifyLastServerUpdate(file_id, last_local_update);
		if(success == -1)
		{
			System.out.println("Modify Last Server update datbase failed");
		}

	}
}