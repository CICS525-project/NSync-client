package DBManager;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import Controller.SendObject;
import Controller.SendObject.EventType;

/**
 * @author yasminf
 *
 */
public class DBController implements Runnable 
{
	private static BlockingQueue<SendObject> eventsQ;
	private static BlockingQueue<SendObject> toSendQ;
	private static BlockingQueue<SendObject> sentQ;

	private static DBEventsQManager eqm;
	private static DBSentQManager sqm;

	public DBController() 
	{
		eventsQ = null;
		toSendQ = null;
		sentQ = null;
	}

	/**
	 * @param event
	 * @param sent
	 * @param toSend
	 */
	public DBController(BlockingQueue<SendObject> event, BlockingQueue<SendObject> sent, BlockingQueue<SendObject> toSend) 
	{
		toSendQ = toSend;
		sentQ = sent;
		eventsQ = event;
	}
	public void run() 
	{
		DBManagerLocal.startDatabase();
		DBManagerLocal.getEpochDate();
		//DBSyncManager.DBClientToServerList();
		/*SendObject obj = new SendObject();
		Date d = new Date();
		obj.setFileName("a.txt");
		obj.setTimeStamp(d);
		System.out.println("Testing Find Conflict _______________________________________________" +DBManagerLocal.findConflict(obj));*/

		eqm = new DBEventsQManager(eventsQ, toSendQ);
		sqm = new DBSentQManager(sentQ);
		new Thread(eqm).start();
		new Thread(sqm).start();
	}
}
