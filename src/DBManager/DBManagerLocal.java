package DBManager;

import java.sql.Statement;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.security.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.math.*;
import java.nio.file.FileSystems;

import Controller.SendObject;

public class DBManagerLocal {
	private static org.hsqldb.server.Server server;
	private static Connection con;

	public DBManagerLocal() {
		server = null;
		con = null;
	}

	public static void startDatabase() {
		server = new org.hsqldb.server.Server();
		server.setAddress("localhost");
		server.setDatabaseName(0, "DB");
		server.setDatabasePath(0, "file:./DB/db");
		server.setPort(9152);

		// set true for debugging
		server.setTrace(false);
		server.setLogWriter(new PrintWriter(System.out));
		server.start();
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace(System.out);
		}
		try {
			// check, if tables exists, if not create them
			Connection con = getConnection();
			DatabaseMetaData md = con.getMetaData();
			ResultSet rs = md.getTables(null, null, "files", null);
			if (!rs.next()) {
				createTables(con);

			}
		} catch (SQLException e) {
			e.printStackTrace(System.out);
		}

	}

	/*
	 * start a local HSQLDB database and check if the tables already exist
	 */
	static void createTables(Connection con) {
		System.out.println("Empty Database. Creating tables...");

		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS files(file_id VARCHAR(200),"
					+ "                    file_path   VARCHAR(200), "
					+ "                    file_name   VARCHAR(200), "
					+ "                    file_hash   VARCHAR(200), "
					+ "					  file_state   VARCHAR(200),"
					+ "                   last_local_update      TIMESTAMP, "
					+ "                   last_server_update     TIMESTAMP, "
					+ "                   user_id	  VARCHAR(200), "
					+ "                   shared_with	  VARCHAR(200), "
					+ "                   primary key(file_id)"
					+ "                  ) ");
			stmt.close();

			// commit the transaction
			con.commit();
			System.out.println("Files Table Created");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * getter for the connection to the HSQLDB database
	 */
	public static Connection getConnection() {
		Connection result = null;
		try {
			if (con != null) {
				if (con.isValid(20)) {
					result = con;
				} else {
					con = DriverManager.getConnection(
							"jdbc:hsqldb:hsql://localhost:9152/DB", "SA", "");
					result = con;
				}
			} else {
				con = DriverManager.getConnection(
						"jdbc:hsqldb:hsql://localhost:9152/DB", "SA", "");
				result = con;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	
	public static java.sql.Timestamp getTimeStamp(Date d) {
		// java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(d.getTime());
	}

	public static java.sql.Timestamp getEpochDate() {
		// java.util.Date today = new java.util.Date();
		DateFormat dateFormat = new SimpleDateFormat("01/01/1970");
		Date date = null;
		try
		{
		date = dateFormat.parse("01/01/1970");
		}
		catch(Exception e)
		{
			//
		}
		long time = date.getTime();
		//java.sql.Timestsamp ep = new java.sql.Timestamp(time);
		System.out.println("the epoch time is tttttttttttttttttttttttttttttttttttttt" + getTimeStamp(date));
		return getTimeStamp(date);
		
	}
	public static String getCurrentState(String file_id) {
		Connection con = getConnection();
		String current_state = "";

		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT file_state FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				current_state = rs.getString(1);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return current_state;
	}
	
	public static String getrowID(String file_path, String file_name) {
		Connection con = getConnection();
		String file_id = "";

		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT file_id FROM files WHERE file_name = ? AND file_path=? AND file_state <> ?");
			ps.setString(1, file_name);
			ps.setString(2, file_path);
			ps.setString(3, "DELETE");
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				file_id = rs.getString(1);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return file_id;
	}

	public static String getFileHash(String file_id) {
		Connection con = getConnection();
		String file_hash = "";

		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT file_hash FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				file_hash = rs.getString(1);

			} else {
				System.out.println("FILE HASH NOT FOUND ______________________________________");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return file_hash;

	}

	/*
	 * generate id for file combination of file_path, file_name, user_name,
	 */
	public static String generateHashID(String file_name, String file_path,
			String user_name, java.sql.Timestamp timestamp)
			throws NoSuchAlgorithmException {
		String hash_string;
		String identity_string = file_name + file_path + user_name
				+ timestamp.toString();
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(identity_string.getBytes(), 0, identity_string.length());
		hash_string = new BigInteger(1, m.digest()).toString(16);
		return hash_string;
	}
	public static java.sql.Timestamp getLastTimeStamp() {
		Connection con = getConnection();
		java.sql.Timestamp last_update = null;
		ResultSet rs = null;

		
		try {
			PreparedStatement ps = con.prepareStatement("SELECT MAX(last_server_update) from files");
			rs = ps.executeQuery();
			if (rs.next()) 
			{
				last_update = rs.getTimestamp(1);
				System.out.println("**DB:SyncManager: getLastTimestamp()***********************************"+last_update);

			}
			else 
			{
				last_update = getEpochDate(); //check last update is not null before sending if its null it means db is 
				System.out.println("**DB:SyncManager: getLastTimestamp()***********************************"+last_update);					//empty and user is logging on device for first time so date set to epoch time
			}
			

		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
		if(last_update == null)
		{
			return getEpochDate();
		}
		else 
		{
			return last_update;
		}
		}
	
	public static ResultSet getOfflineChanges() {
		Connection con = getConnection();
		String file_id = "";
		java.sql.Timestamp last_update;
		ResultSet rs = null;

		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT * FROM files WHERE last_local_update > last_server_update OR file_state = 'DELETE' OR file_state = 'RENAME'");
			rs = ps.executeQuery();
			if (rs.next()) 
			{
				return rs;
			}
			else 
			{
				return null; 
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return rs;
	}
	
	public static String getFileName(String file_id) {
		Connection con = getConnection();
		String file_name = "";

		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT file_name FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				file_name = rs.getString(1);

			} else {
				System.out
						.println("FILE NAME NOT FOUND N______________________________________");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return file_name;

	}
	public static int localinsert(String file_id, String file_path,
			String file_name, String file_hash, String file_state,
			String user_id, java.sql.Timestamp last_local_update) {
		int result = -1;
		Connection con = getConnection();

		if (!isIDInDB(file_id)) {
			try {
				PreparedStatement ps = con
						.prepareStatement("INSERT INTO files(file_id, file_path, file_name, file_hash, file_state, last_local_update, user_id) VALUES (?,?,?,?,?,?,?)");
				ps.setString(1, file_id);
				ps.setString(2, file_path);
				ps.setString(3, file_name);
				ps.setString(4, file_hash);
				ps.setString(5, file_state);
				ps.setTimestamp(6, last_local_update);
				ps.setString(7, user_id);
				result = ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;
			}
		}

		else {
			result = -1;
		}

		return result;
	}

	

	public static int localModify(String file_id, String file_hash,
			String file_state, java.sql.Timestamp last_local_update) {
		int result = -1;
		Connection con = getConnection();

		if (isIDInDB(file_id)) {
			try {
				PreparedStatement ps = con
						.prepareStatement("UPDATE files SET last_local_update = ?, file_hash = ?, file_state = ? WHERE file_id = ?");
				ps.setTimestamp(1, last_local_update);
				ps.setString(2, file_hash);
				ps.setString(3, file_state);
				ps.setString(4, file_id);
				result = ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;
			}
		}

		return result;
	}

	public static int localRename(String file_id, String file_name, String new_file_name, String file_path, String state,
			java.sql.Timestamp last_local_update, boolean is_folder) {
		int result = -1;
		Connection con = getConnection();
		PreparedStatement ps = null;
		String root_path = "";

		// update the renamed folder/file entry
		try {
			ps = con.prepareStatement("UPDATE files "
					+ " SET last_local_update = ? " + "    ,file_name = ? "
					+ "    ,file_state = ? " + "WHERE file_path = ? "
					+ "  AND file_name = ?");
			ps.setTimestamp(1, last_local_update);
			ps.setString(2, new_file_name);
			ps.setString(3, state);
			ps.setString(4, file_path);
			ps.setString(5, file_name);
			result = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			result = -1;
		}
		if (is_folder) {
			// for folder entry cascade rename to child entries
			if (file_path.trim().length() == 0) {
				root_path = "";
			} else {
				root_path = file_path + FileSystems.getDefault().getSeparator();
			}
			try {
				System.out
						.println("________________________________________________________________________");
				System.out
						.println("root_path + file_name + FileSystems.getDefault().getSeparator() ==> "
								+ root_path
								+ file_name
								+ FileSystems.getDefault().getSeparator());
				System.out
						.println("root_path + new_file_name + FileSystems.getDefault().getSeparator() ==> "
								+ root_path
								+ new_file_name
								+ FileSystems.getDefault().getSeparator());
				System.out
						.println("root_path + file_name + FileSystems.getDefault().getSeparator() + % ==> "
								+ root_path
								+ file_name
								+ FileSystems.getDefault().getSeparator() + "%");

				ps = con.prepareStatement("UPDATE files "
						+ " SET file_path = replace(file_path,  ?, ?) "
						+ "WHERE file_path LIKE ?");
				ps.setString(1, root_path + file_name
						+ FileSystems.getDefault().getSeparator());
				ps.setString(2, root_path + new_file_name
						+ FileSystems.getDefault().getSeparator());
				ps.setString(3, root_path + file_name
						+ FileSystems.getDefault().getSeparator() + "%");
				result = ps.executeUpdate();
				System.out.println("________________________________________________________________________");
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;
			}
			try {
				System.out
						.println("________________________________________________________________________");

				System.out.println("root_path + file_name ==> " + root_path
						+ file_name);
				System.out.println("root_path + new_file_name ==> " + root_path
						+ new_file_name);
				System.out.println("root_path + file_name ==> " + root_path
						+ file_name);

				ps = con.prepareStatement("UPDATE files "
						+ " SET file_path = replace(file_path,  ?, ?) "
						+ "WHERE file_path = ?");
				ps.setString(1, root_path + file_name);
				ps.setString(2, root_path + new_file_name);
				ps.setString(3, root_path + file_name);
				result = ps.executeUpdate();
				System.out.println("________________________________________________________________________");
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;
			}
		}

		return result;
	}

	

	
	public static int localModifyLastServerUpdate(String file_id, java.sql.Timestamp last_server_update) {
		int result = -1;
		String current_state = "";
				Connection con = getConnection();
		if (isIDInDB(file_id)) {
			current_state = getCurrentState(file_id);
			if(!current_state.equalsIgnoreCase("DELETE"))
			{
				try {
					PreparedStatement ps = con.prepareStatement("UPDATE files SET last_server_update = ?, file_state = ? WHERE file_id = ?");
					ps.setTimestamp(1, last_server_update);
					ps.setString(2, "");
					ps.setString(3, file_id);
					result = ps.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
					result = -1;
				}
			}
			else //if it is a delete event remove from db when you receive last update from server
			{
				try {
					PreparedStatement ps = con.prepareStatement("DELETE FROM files WHERE file_id = ?");
					ps.setString(1, file_id);
					result = ps.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
					result = -1;
				}
			}
		}
		return result;
		}

	public static int localShare(String file_id, String shared_withID)
	{
		int result = -1;

		Connection con = getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE files SET share_with = ? WHERE file_id = ?");
			ps.setString(1, shared_withID);
			ps.setString(2, file_id);
			result = ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			result = -1;
		}
		return result;
	}
	
	public static int localDelete(String file_id) {
		int result = -1;

		Connection con = getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("UPDATE files SET file_state = ? WHERE file_id = ?");
			ps.setString(1, "DELETE");
			ps.setString(2, file_id);
			result = ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			result = -1;
		}
		return result;
	}

	public static int localRemove(String file_id) {
		int result = -1;

		Connection con = getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("DELETE FROM  files WHERE file_id = ?");
			ps.setString(1, file_id);
			result = ps.executeUpdate();
			System.out.println("DB Result is ***************************************************"+result);

		} catch (SQLException e) {
			e.printStackTrace();
			result = -1;
		}
		return result;
	}

	// maintain hierarchy for events.
		public static String setNewState(String event, String current_state) {
			String new_state = "";

			// delete has highest hierarchy if delete event then set to delete
			if (event.equalsIgnoreCase("DELETE")) {
				new_state = "DELETE";

			}
			
			else if (event.equalsIgnoreCase("SHARE")) {
				new_state = current_state;
			}


			// modify has second highest hierarchy if delete event then set to
			// delete
			else if (event.equalsIgnoreCase("MODIFY")) {
				if (current_state.equalsIgnoreCase("DELETE")) {
					new_state = current_state;
				} else {
					new_state = "MODIFY";
				}
			}

			// CREATE has third highest hierarchy
			else if (event.equalsIgnoreCase("CREATE")) {
				if ( (current_state.equalsIgnoreCase("MODIFY")) || (current_state.equalsIgnoreCase("DELETE")) ) {
					new_state = current_state;
				} else {
					new_state = "CREATE";
				}
			}

			// RENAME lowest in hierarchy
			else if (event.equalsIgnoreCase("RENAME")) {
				if ((current_state.equalsIgnoreCase("MODIFY")) || (current_state.equalsIgnoreCase("DELETE")) || (current_state.equalsIgnoreCase("CREATE"))) {
					new_state = current_state;
				} 
				
				else {
					new_state = "RENAME";
				}
			}
			
			return new_state;
		}

	
	public static boolean isIDInDB(String file_id) {
		boolean result = false;
		Connection con = getConnection();
		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT * FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = true;
			} else {
				result = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	

	public static boolean isFileInDB(String file_name, String file_path) {
		boolean result = false;
		Connection con = getConnection();
		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT * FROM files WHERE file_path = ? AND file_name = ?");
			ps.setString(1, file_path);
			ps.setString(2, file_name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result = true;
			} else {
				result = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public static boolean fileHashChanged(String file_id, String current_file_hash) {
		boolean result = false;
		String prev_hash = "";
		Connection con = getConnection();
		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT file_hash FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				prev_hash = rs.getString(1);
				// check if prev_hash is null or current_file_hash
				if (!prev_hash.equals(current_file_hash)) {
					result = true;
				} else {
					result = false;
				}
			}

			else {
				System.out
						.println("FILE ID NOT FOUND FOR HASH COMPARISON ______________________________________");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public static boolean fileNameChanged(String file_id, String new_file_name) {
		boolean result = false;
		String prev_file_name = "";
		Connection con = getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("SELECT file_name FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				prev_file_name = rs.getString(1);
				// check if prev_hash is null or current_file_hash
				if (!prev_file_name.equals(new_file_name)) {
					result = true;
				} else {
					result = false;
				}
			}

			else {
				result = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	
	
	
	public static boolean findConflict(SendObject obj) {
		java.sql.Timestamp server_ts = getTimeStamp(obj.getTimeStamp());
		java.sql.Timestamp last_local_ts = null;
		java.sql.Timestamp last_server_ts = null;
		String file_id = obj.getID();
                String file_name = obj.getFileName();
                String file_path = obj.getFilePath();
		boolean conflict = false;
		Connection con = getConnection();
                System.out.println("\n^^^^^^^^^conflict data file_id^^^^^^");
                System.out.println("file id: " + file_id);
                
		try {
			PreparedStatement ps = con.prepareStatement("SELECT last_local_update, last_server_update FROM files WHERE file_name = ? AND file_path = ?");
			ps.setString(1, file_name);
                        ps.setString(2, file_path);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				last_local_ts = rs.getTimestamp(1);
				last_server_ts = rs.getTimestamp(2);
                                System.out.println("\n^^^^^^^^^conflict data^^^^^^");
                                System.out.println("last local TS: " + last_local_ts);
                                System.out.println("last server ts" + last_server_ts);
                                System.out.println("server TS: " + server_ts);
                                boolean a = last_local_ts != server_ts;
                                boolean b = last_server_ts != server_ts;
                                boolean c = last_local_ts != last_server_ts;
				if ((a || b) && (a || c) && (b||c)) {
					System.out.println("CONFLICT EXISTS Last local Update______"
									+ last_local_ts);
					System.out.println("CONFLICT EXISTS Last server Update______"
									+ last_server_ts);
					System.out.println("CONFLICT EXISTS Last on server Update______"
									+ server_ts);
					conflict = true;
				}

			} else {
				System.out.println("DBManager---------------------------------------------OBJECT DOES NOT EXIST IN DB FILE MATCH NOT FOUND");
			}

		}

		catch (SQLException e) {
			e.printStackTrace();
		}

		return conflict;

	}

	
	
	

	public static String findEventfromServer(String file_id, String new_hash,
			String new_file_name) {

		String what_event = "";

		// file exists in db proceed to check to what is the event.
		if (isIDInDB(file_id)) {

			// if file hash changed but file name is the same event is modify
			if (fileHashChanged(file_id, new_hash)
					&& (!fileNameChanged(file_id, new_file_name))) {
				what_event = "MODIFY";
			}

			// if file hash has not changed and file name has changed then event
			// is rename
			else if (!fileHashChanged(file_id, new_hash)
					&& (fileNameChanged(file_id, new_file_name))) {
				what_event = "RENAME";
			}

			// if file hash has changed and file name has changed then event are
			// modify followed by rename
			else if (fileHashChanged(file_id, new_hash)
					&& (fileNameChanged(file_id, new_file_name))) {
				what_event = "MODIFYRENAME";
			}

		}

		else if (getCurrentState(file_id).equalsIgnoreCase("DELETE")) {
			what_event = "DELETE";
		}

		// file doesn't exist in database thus must be a create event.
		else {
			what_event = "CREATE";
		}
		return what_event;
	}

        public static java.sql.Timestamp getTSfromDB(String file_id) {
            Connection con = getConnection();
		java.sql.Timestamp TS = null;

		try {
			PreparedStatement ps = con
					.prepareStatement("SELECT last_local_update FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				TS = rs.getTimestamp(1);

			} else {
				System.out.println("FILE TIMESTAMP FOUND ______________________________________");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return TS;
    }
        
}
