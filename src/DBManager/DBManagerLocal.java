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
import java.math.*;
import java.nio.file.FileSystems;

public class DBManagerLocal 
{
	private static org.hsqldb.server.Server server;
	private static Connection con;
	/*
	 * start a local HSQLDB database and check if the tables already exist
	 */
	public DBManagerLocal()
	{
		server = null;
		con = null;
	}

	public static void startDatabase() 
	{
		server = new org.hsqldb.server.Server();
		server.setAddress("localhost");
		server.setDatabaseName(0, "DB");
		server.setDatabasePath(0, "file:./DB/db");
		server.setPort(9152);


		// set true for debugging
		server.setTrace(false);
		server.setLogWriter(new PrintWriter(System.out));
		server.start();
		try 
		{
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace(System.out);
		}
		try 
		{
			// check, if tables exists, if not create them
			Connection con = getConnection();
			DatabaseMetaData md = con.getMetaData();
			ResultSet rs = md.getTables(null, null, "files", null);
			if (!rs.next()) 
			{
				createTables(con);

			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace(System.out);
		}

	}

	static void createTables(Connection con)
	{
		System.out.println("Empty Database. Creating tables...");

		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS files(file_id VARCHAR(200),"
					+"                    file_path   VARCHAR(200), "
					+"                    file_name   VARCHAR(200), "
					+"                    file_hash   VARCHAR(200), "
					+ "                   last_local_update      TIMESTAMP, "
					+ "                   last_server_update     TIMESTAMP, "
					+ "                   user_id	  VARCHAR(200), "
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
	public static Connection getConnection() 
	{
		Connection result = null;
		try 
		{
			if (con != null)
			{
				if(con.isValid(20))
				{
					result = con;
				}
				else
				{
					con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:9152/DB", "SA", "");
					result = con;
				}
			}
			else
			{
				con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:9152/DB", "SA", "");
				result = con;
			}
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static int localinsert(String file_id, String file_path, String file_name, String file_hash, String user_id, java.sql.Timestamp last_local_update) 
	{
		int result = -1;
		Connection con = getConnection();


		if (!isIDInDB(file_id)) 
		{
			try {
				PreparedStatement ps = con
						.prepareStatement("INSERT INTO files(file_id, file_path, file_name, file_hash, last_local_update, user_id) VALUES (?,?,?,?,?,?)");
				ps.setString(1, file_id);
				ps.setString(2, file_path);
				ps.setString(3, file_name);
				ps.setString(4, file_hash);
				ps.setTimestamp(5, last_local_update);
				ps.setString(6, user_id);
				result = ps.executeUpdate();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				result = -1;
			}
		}

		else 
		{
			result = -1;
		}

		return result;
	}


	public static int localModify(String file_id, String file_hash, java.sql.Timestamp last_local_update) {
		int result = -1;
		Connection con = getConnection();

		if (isIDInDB(file_id)) {
			try {
				PreparedStatement ps = con
						.prepareStatement("UPDATE files SET last_local_update = ?, file_hash = ? WHERE file_id = ?");
				ps.setTimestamp(1, last_local_update);
				ps.setString(2, file_hash);
				ps.setString(3,  file_id);
				result = ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;
			}
		}

		return result;
	}

	public static int localRename(String file_id, String file_name, String new_file_name, String file_path, java.sql.Timestamp last_local_update, boolean is_folder) 
	{
		int result = -1;
		Connection con = getConnection();
		PreparedStatement ps = null;
		String root_path = "";

		//update the renamed folder/file entry
		try 
		{
					ps = con.prepareStatement("UPDATE files "
					+ " SET last_local_update = ? "
					+ "    ,file_name = ? "
					+ "WHERE file_path = ? "
					+ "  AND file_name = ?");
			ps.setTimestamp(1, last_local_update);
			ps.setString(2, new_file_name);
			ps.setString(3, file_path);
			ps.setString(4, file_name);
			result = ps.executeUpdate();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			result = -1;
		}
		if (is_folder) 
		{
			//for folder entry cascade rename to child entries
			if(file_path.trim().length() == 0)
			{
				root_path = "";
			}
			else
			{
				root_path = file_path + FileSystems.getDefault().getSeparator();
			}
			try 
			{
				System.out.println("________________________________________________________________________");
				System.out.println("root_path + file_name + FileSystems.getDefault().getSeparator() ==> "+root_path + file_name + FileSystems.getDefault().getSeparator() );
				System.out.println("root_path + new_file_name + FileSystems.getDefault().getSeparator() ==> " + root_path + new_file_name + FileSystems.getDefault().getSeparator() );
				System.out.println("root_path + file_name + FileSystems.getDefault().getSeparator() + % ==> " + root_path + file_name + FileSystems.getDefault().getSeparator() + "%");
				
				ps = con.prepareStatement("UPDATE files "
						+ " SET file_path = replace(file_path,  ?, ?) "
						+ "WHERE file_path LIKE ?");
				ps.setString(1, root_path + file_name + FileSystems.getDefault().getSeparator() );
				ps.setString(2, root_path + new_file_name + FileSystems.getDefault().getSeparator() );
				ps.setString(3, root_path + file_name + FileSystems.getDefault().getSeparator() + "%");
				result = ps.executeUpdate();
				System.out.println("________________________________________________________________________");
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				result = -1;
			}
			try 
			{
				System.out.println("________________________________________________________________________");

				System.out.println("root_path + file_name ==> "+root_path + file_name );
				System.out.println("root_path + new_file_name ==> " + root_path + new_file_name );
				System.out.println("root_path + file_name ==> " + root_path + file_name);
				
				ps = con.prepareStatement("UPDATE files "
						+ " SET file_path = replace(file_path,  ?, ?) "
						+ "WHERE file_path = ?");
				ps.setString(1, root_path + file_name);
				ps.setString(2, root_path + new_file_name);
				ps.setString(3, root_path + file_name);
				result = ps.executeUpdate();
				System.out.println("________________________________________________________________________");
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				result = -1;
			}
		}

		return result;
	}

	public static int localModifyLastServerUpdate(String file_id, java.sql.Timestamp last_server_update) {
		int result = -1;
		Connection con = getConnection();
		if (isIDInDB(file_id)) {
			try {
				PreparedStatement ps = con
						.prepareStatement("UPDATE files SET last_server_update = ? WHERE file_id = ?");
				ps.setTimestamp(1, last_server_update);
				ps.setString(2, file_id);
				result = ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				result = -1;
			}
		}
		return result;
	}

	public static int localDelete(String file_id) {
		int result = -1;


		Connection con = getConnection();
		try {
			PreparedStatement ps = con
					.prepareStatement("DELETE FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			result = ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			result = -1;
		}
		return result;
	}

	public static boolean isIDInDB(String file_id) {
		boolean result = false;
		Connection con = getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM files WHERE file_id = ?");
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
			PreparedStatement ps = con.prepareStatement("SELECT * FROM files WHERE file_path = ? AND file_name = ?");
			ps.setString(1, file_path);
			ps.setString(2,  file_name);
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
		String prev_hash ="";
		Connection con = getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("SELECT file_hash FROM files WHERE file_id = ?");
			ps.setString(1, file_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) 
			{
				prev_hash = rs.getString(1);
				//check if prev_hash is null or current_file_hash
				if(!prev_hash.equals(current_file_hash))
				{
					result = true;
				}
				else
				{
					result = false;
				}
			} 

			else 
			{
				result = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}



	public static java.sql.Timestamp getTimeStamp(Date d) 
	{
		// java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(d.getTime());
	}

	/*
	 * generate id for file combination of file_path, file_name, user_name,
	 */
	public static String generateHashID(String file_name, String file_path, String user_name, java.sql.Timestamp timestamp) throws NoSuchAlgorithmException
	{
		String hash_string;
		String identity_string = file_name+file_path+user_name+timestamp.toString();
		MessageDigest m=MessageDigest.getInstance("MD5");
		m.update(identity_string.getBytes(),0,identity_string.length());
		hash_string = new BigInteger(1,m.digest()).toString(16);
		return hash_string;
	}

	public static String getrowID(String file_path, String file_name)
	{
		Connection con = getConnection();
		String file_id="";

		try {
			PreparedStatement ps = con.prepareStatement("SELECT file_id FROM files WHERE file_name = ? AND file_path=?");
			ps.setString(1, file_name);
			ps.setString(2, file_path);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) 
			{
				file_id = rs.getString(1);

			} 


		} catch (SQLException e) {
			e.printStackTrace();
		}	


		return file_id;
	}
}
