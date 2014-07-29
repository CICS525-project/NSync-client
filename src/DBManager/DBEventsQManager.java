package DBManager;

import java.util.concurrent.BlockingQueue;

import Controller.SendObject;
import Controller.SendObject.EventType;

public class DBEventsQManager extends DBManagerLocal implements Runnable{

	private static BlockingQueue<SendObject> eventsQ;
	private static BlockingQueue<SendObject> toSendQ;
	public DBEventsQManager(BlockingQueue<SendObject> events, BlockingQueue<SendObject> toSend)
	{
		super();
		toSendQ = toSend;
		eventsQ = events;
	}
	public void run() 
	{
		SendObject inObj = null;
		SendObject outObj = null;
		while(true)
		{
			try 
			{
				inObj = eventsQ.take();
				outObj = processQueue(inObj);
				toSendQ.put(outObj);           
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public static SendObject processQueue(SendObject obj) // process queue in events queue class
	{
		String file_id="";
		int success = -1;

		String file_path = obj.getFilePath();
		String file_name = obj.getFileName();
		String file_hash = obj.getHash();
		java.sql.Timestamp last_local_update =  getTimeStamp(obj.getTimeStamp());
		String userID = obj.getUserID();
		String new_file_name;
		EventType event = obj.getEvent();

		if(event == EventType.Create)
		{
			try
			{
				file_id = generateHashID(file_name, file_path, userID, last_local_update);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println("Inserting file -----------------------------------------------------");
			success = localinsert(file_id, file_path, file_name, file_hash, userID,  last_local_update);
			if(success!=-1)
			{

				obj.setID(file_id);
			}
		}
		else if(event == EventType.Modify)
		{

			file_id = getrowID(file_path, file_name);
			if(fileHashChanged(file_id, file_hash))
			{
				System.out.println("Modifying file -----------------------------------------------------");
				success = localModify(file_id, file_hash, last_local_update);

			}
			//else ignore event;
		}
		else if(event == EventType.Rename)
		{
			file_id = getrowID(file_path, file_name);
			new_file_name = obj.getNewFileName();
			if(obj.isIsAFolder())
			{
				System.out.println("Renaming folder -----------------------------------------------------");
				success = localRename(file_id, file_name, new_file_name, file_path, last_local_update, true);
				//				if(obj.getFilePath()!=null)
				//				{
				//					new_file_path = obj.getFilePath()+"\\"+obj.getNewFileName();
				//					old_file_path = obj.getFilePath()+"\\"+obj.getFileName();
				//				}
				//
				//				else //root folder so do not need to concatenate path;
				//				{
				//					new_file_path = obj.getNewFileName()+"\\";
				//					old_file_path = obj.getFileName()+"\\";
				//				}
			}
			else
			{			
				System.out.println("Renaming file -----------------------------------------------------");
				success = localRename(file_id, file_name, new_file_name, file_path, last_local_update, false);
			}
		}
		else if(event == EventType.Delete)
		{
			file_id = getrowID(file_path, file_name);
			System.out.println("Deleting file -----------------------------------------------------");
			success = localDelete(file_id);	
		}

		obj.setID(file_id);
		obj.setEnteredIntoDB(true);
		return obj;

	}

}
