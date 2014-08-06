package DBManager;

import java.util.concurrent.BlockingQueue;

import Controller.NSyncClient;
import Controller.SendObject;
import Controller.SendObject.EventType;
import java.sql.Timestamp;

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

				if(outObj!=null && !(outObj.isIsAFolder()))
				{
					NSyncClient.toSendQ.put(outObj);
					System.out.println("ADDED TO toSendQ*********************************************************" + outObj.getID());
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
			
			System.out.println("**DBMANAGER: EventQManager  **********************************************************");
			String file_id=obj.getID();
			int success = -1;
			String file_path = obj.getFilePath();
			String file_name = obj.getFileName();
			String file_hash = obj.getHash();
			String shared_with_userID = obj.getSharedWith();
			java.sql.Timestamp last_local_update =  getTimeStamp(obj.getTimeStamp());
			String userID = obj.getUserID();
			String new_file_name;
			EventType event = obj.getEvent();
			String string_event = obj.getEvent().toString().toUpperCase();
			String new_state = "";
			boolean ignore_event = false;
			String check_id="";
			String check_hash = "";
			
			
			check_id = getrowID(file_path, file_name);
			check_hash = getFileHash(check_id);
                        java.sql.Timestamp check_TS = getTSfromDB(check_id);
			
			 //get id from file_path and filename 
			//then get hash of that file 
			//if hash is the same ignore event 
			if(check_hash.equals(file_hash) && event != EventType.Rename )
			{
				ignore_event = true; 
				System.out.println("**DBMANAGER: EventQManager : Ignoring Event **********************************************************");
				
			}

	         if(!ignore_event)
	         {
			
			
				if(event == EventType.Create && !(obj.isIsAFolder()))
				{
					if(!isFileInDB(file_path, file_name))
					{
					try
					{
						file_id = generateHashID(file_name, file_path, userID, last_local_update);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				
					success = localinsert(file_id, file_path, file_name, file_hash, string_event, userID, last_local_update) ;
					if(success!=-1)
					{

						
						obj.setID(file_id);
						System.out.println("Inserting file local-----------------------------------------------------"+file_id);
					}
					}
					//else 
					//{
						//ignore event false event from from blob
					//}
				
				}
				else if(event == EventType.Modify && !(obj.isIsAFolder()))
				{

					file_id = getrowID(file_path, file_name);
					new_state = setNewState(string_event, getCurrentState(file_id));
					if(file_hash!=null && file_id!=null)
						
					{
							
						
						if(fileHashChanged(file_id, file_hash) && check_TS.before(last_local_update))
						{
							System.out.println("Modifying file local -----------------------------------------------------"+file_id);
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

					System.out.println("Renaming file local-----------------------------------------------------"+file_id);
					success = localRename(file_id, file_name, new_file_name, file_path, new_state, last_local_update, false);
					

				}
				
				else if(event == EventType.Share && !(obj.isIsAFolder()))
				{
					file_id = getrowID(file_path, file_name);
					System.out.println("Updating Local Share -----------------------------------------------------"+file_id);
					success = localShare(file_id, shared_with_userID);	
				}

				else if(event == EventType.Delete && !(obj.isIsAFolder()))
				{
					file_id = getrowID(file_path, file_name);
					System.out.println("Deleting file -----------------------------------------------------"+file_id);
					success = localDelete(file_id);	
				}
				obj.setEnteredIntoDB(true);
				obj.setID(file_id);
                                if (success != -1){
                                    return obj;
                                       
                                }
	         }
	         
	        	 return null;
	        
	      
		}
		
	}


