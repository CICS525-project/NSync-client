package DBManager;

import java.sql.ResultSet;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import Controller.NSyncClient;
import Controller.SendObject;
import Controller.SendObject.EventType;
import Controller.SendObject;
public class DBSyncManager extends DBManagerLocal{

	private static BlockingQueue<SendObject> sentQ;
	

	public DBSyncManager()
	{
		super();

	}

	public static void DBClientToServerList()
	{
		ResultSet rs = getOfflineChanges();
		SendObject sobj=null;
		try
		{
			while (rs.next()) 
			{
				String file_id = rs.getString("file_id");
				String file_path = rs.getString("file_path");
				String file_name = rs.getString("file_name");
				String file_hash = rs.getString("file_hash");
				String file_state = rs.getString("file_state");
				java.sql.Timestamp last_local= rs.getTimestamp("last_local_update");
				java.sql.Timestamp last_server= rs.getTimestamp("last_server_update");
				Date date = new Date(last_local.getTime());
				String user_id = rs.getString("user_id");
				
				
				System.out.println(file_id + "\t" + file_path +
						"\t" + file_name + "\t" + file_hash +
						"\t" + file_state + "\t" + last_local + "\t" + last_server + "\t" + user_id);
				 sobj = new SendObject(file_id, file_name, file_path, toEvent(file_state), date, false, "");
                
				if(sobj!=null)
				{
					NSyncClient.toSendQ.put(sobj);
				}
			}
		} catch (Exception e ) {
			e.printStackTrace(System.out);
		}

	}

	public static SendObject processObjFromServer(SendObject obj) // process queue in events queue class
	{
		String file_id = obj.getID();
		int success = -1;

		String file_path = obj.getFilePath();
		String file_name = obj.getFileName();
		String file_hash = obj.getHash();
		java.sql.Timestamp last_time_stamp =  getTimeStamp(obj.getTimeStamp());
		String userID = obj.getUserID();
		String new_file_name;
		Controller.SendObject.EventType event = obj.getEvent();
		String string_event = "";
		String new_state = "";
		String current_file_name = "";


		

			if(event==null)
			{
				string_event = findEventfromServer(file_id, file_hash, file_name);
			}
			else
			{
				string_event = event.toString();
			}
			
			
			
			System.out.println("**DBMANAGER: SyncQManager **********************************************************");
			if(string_event.equalsIgnoreCase("CREATE") && !(obj.isIsAFolder()))
			{
				try
				{
					file_id = generateHashID(file_name, file_path, userID, last_time_stamp);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				success = localinsert(file_id, file_path, file_name, file_hash, string_event, userID, last_time_stamp) ;
				if(success!=-1)
				{

					System.out.println("Inserting file from server-----------------------------------------------------"+file_id);
					obj.setID(file_id);
				}
			}
			else if(string_event.equalsIgnoreCase("MODIFY") && !(obj.isIsAFolder()))
			{


				new_state = setNewState(string_event, getCurrentState(file_id));
				System.out.println("Modifying file from server -----------------------------------------------------"+file_id);
				success = localModify(file_id, file_hash, new_state, last_time_stamp);


			}
			else if(string_event.equalsIgnoreCase("RENAME")&& !(obj.isIsAFolder()))
			{

				new_state = setNewState(string_event, getCurrentState(file_id));
				current_file_name = getFileName(file_id);
				System.out.println("Renaming file from server-----------------------------------------------------" +file_id);
				success = localRename(file_id, current_file_name, file_name, file_path, new_state, last_time_stamp, false);


			}

			else if(string_event.equalsIgnoreCase("MODIFYRENAME")&& !(obj.isIsAFolder()))
			{

				new_state = setNewState(string_event, getCurrentState(file_id));
				current_file_name = getFileName(file_id);
				System.out.println("Renaming AND MODIFYING file from server -----------------------------------------------------"+file_id);
				success = localModify(file_id, file_hash, new_state, last_time_stamp);
				success = localRename(file_id, current_file_name, file_name, file_path, new_state, last_time_stamp, false);


			}
			
			else if(string_event.equalsIgnoreCase("DELETE")&& !(obj.isIsAFolder()))
			{

				System.out.println("Deleting file -----------------------------------------------------");
				success = localRemove(file_id);	
			}

			else if(string_event.equalsIgnoreCase("SHARE")&& !(obj.isIsAFolder()))
			{

				System.out.println("SHARING file FROM SERVER IGNORE -----------------------------------------------------");
				//DO NOTHING 	
			}

			localModifyLastServerUpdate(file_id, last_time_stamp);
			obj.setEnteredIntoDB(true);
			obj.setID(file_id);
			obj.setEvent(toEvent(string_event));
			
			
			System.out.println("UPDATED OBJECT FROM SERVER-----------------------------------------------------"+file_id);
			System.out.println("UPDATED OBJECT FROM SERVER EVENT SET TO -----------------------------------------------------"+string_event);
	return obj;	
	}



	public static EventType toEvent(String str_event)
	{
		EventType ev = null;
		if(str_event.equalsIgnoreCase("Create"))
		{
			ev = EventType.Create;
		}
		else if (str_event.equalsIgnoreCase("Modify"))
		{
			ev = EventType.Modify;
		}
		else if (str_event.equalsIgnoreCase("Rename"))
		{
			ev = EventType.Rename;
		}
		else if (str_event.equalsIgnoreCase("Delete"))
		{
			ev = EventType.Delete;
		}
		return ev;
	}


}
