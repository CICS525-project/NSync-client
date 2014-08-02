package DBManager;

import java.util.concurrent.BlockingQueue;

import Controller.NSyncClient;
import Controller.SendObject;

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

		//eqm = new DBEventsQManager(eventsQ, toSendQ);
		//sqm = new DBSentQManager(sentQ);
		
		eqm = new DBEventsQManager(eventsQ, toSendQ);
		sqm = new DBSentQManager(sentQ);

		
		new Thread(eqm).start();
		new Thread(sqm).start();
	}
}
