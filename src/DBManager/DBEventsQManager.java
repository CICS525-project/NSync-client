package DBManager;

import java.util.concurrent.BlockingQueue;

import Controller.NSyncClient;
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

				if(outObj!=null)
				{
					NSyncClient.toSendQ.put(outObj);
					System.out.println("Item added to tosendq. Size is*********************************************************" + NSyncClient.toSendQ.size());
				}
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
			//System.out.println("The file path in DB Events now is ***************************************" + obj.getFilePath());
			String file_name = obj.getFileName();
			String file_hash = obj.getHash();
			java.sql.Timestamp last_local_update =  getTimeStamp(obj.getTimeStamp());
			String userID = obj.getUserID();
			String new_file_name;
			EventType event = obj.getEvent();
			String string_event = obj.getEvent().toString();
			String new_state = "";
			if(!obj.isIsAFolder())
			{		
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
					success = localinsert(file_id, file_path, file_name, file_hash, string_event, userID, last_local_update) ;
					if(success!=-1)
					{

						System.out.println("Inserting file -----------------------------------------------------"+file_id);
						obj.setID(file_id);
					}
				}
				else if(event == EventType.Modify)
				{

					file_id = getrowID(file_path, file_name);
					new_state = setNewState(string_event, getCurrentState(file_id));
					if(file_hash!=null && file_id!=null)
					{
						if(fileHashChanged(file_id, file_hash))
						{
							System.out.println("Modifying file -----------------------------------------------------");
							success = localModify(file_id, file_hash, new_state, last_local_update);

						}
					}
					//else ignore event;
				}
				else if(event == EventType.Rename)
				{
					file_id = getrowID(file_path, file_name);
					new_state = setNewState(string_event, getCurrentState(file_id));
					new_file_name = obj.getNewFileName();
					/*if(obj.isIsAFolder()) no longer keeping information for folders
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
					}*/

					System.out.println("Renaming file -----------------------------------------------------");
					success = localRename(file_id, file_name, new_file_name, file_path, new_state, last_local_update, false);
					

				}
				else if(event == EventType.Delete)
				{
					file_id = getrowID(file_path, file_name);
					System.out.println("Deleting file -----------------------------------------------------");
					success = localDelete(file_id);	
				}


				obj.setEnteredIntoDB(true);
				System.out.println("Inserting file id is-----------------------------------------------------"+file_id);
				return obj;
			}

			else 
			{
				//is a folder do nothing. 
				System.out.println("OBJECT IS A FOLDER DB MANAGER NOT INSERTING __________________________________________________________");
				return null;
			}
		}
	}


