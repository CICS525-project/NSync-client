package DBManager;

import java.sql.ResultSet;
import java.util.concurrent.BlockingQueue;

import Controller.SendObject;

public class DBSyncManager extends DBManagerLocal{
	
private static BlockingQueue<SendObject> sentQ;
	
	public DBSyncManager()
	{
		super();
		
	}
/*
	public static void DBClientToServerList()
	{
		ResultSet rs = getOfflineChanges();
		 while (results.next()) {
	            String file_id = rs.getString("file_id");
	            String file_path = rs.getString("file_path");
	            String file_name = rs.getString("file_name");
	            String file_hash = rs.getString("file_state");
	            java.sql.Timestamp = rs.getTimestamp("last_local")
	            float price = rs.getFloat("PRICE");
	            int sales = rs.getInt("SALES");
	            int total = rs.getInt("TOTAL");
	            System.out.println(coffeeName + "\t" + supplierID +
	                               "\t" + price + "\t" + sales +
	                               "\t" + total);
	        }
	    } catch (SQLException e ) {
	        JDBCTutorialUtilities.printSQLException(e);
	    }
		
	}
*/
}
