package datastore.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import datastore.server.PersistentHashMapServer;

/**
 * Class used to get and set the mapping between session ids <-> user ids, using
 * a pesistent {@link HashMap}. The server runs on the 
 * @author giannis
 *
 */
public class PersistentHashMapClient {
	
	private String host="83.212.104.253";
	private int port=4545;
	
	public PersistentHashMapClient() {
		
	}
	
	public PersistentHashMapClient(String host, int port){
		this.host=host;
		this.port=port;
	}
	
	/**
	 * This methods adds another mapping to the server.
	 * @param session
	 * @param userId
	 */
	public void add(String session, int userId){
		this.sendPacket(PersistentHashMapServer.TYPE_QUERY_ADD, session+":"+userId);
	}
	
	/**
	 * Returns the session for a given userid.
	 * @param userId
	 * @return
	 */
	public String getSession(int userId){
		return this.sendPacket(PersistentHashMapServer.TYPE_QUERY_GET_SESSION, new Integer(userId).toString());
	}

	/**
	 * Returns the userid for a specified session.
	 * @param session
	 * @return
	 */
	public int getUserId(String session){
		if(!session.equals("null"))
			return new Integer(this.sendPacket(PersistentHashMapServer.TYPE_QUERY_GET_ID, session));
		else
			return -1;
	}
	
	/**
	 * Remove a mapping from the HashMap based on the session id.
	 * @param session
	 */
	public void remove(String session){
		this.sendPacket(PersistentHashMapServer.TYPE_QUERY_DEL_SESSION, session);
	}
	
	/**
	 * Remove a mapping from the HashMap based on the user id.
	 * @param userid
	 */
	public void remove(Integer userid){
		this.sendPacket(PersistentHashMapServer.TYPE_QUERY_DEL_ID, userid.toString());
	}
	
	/**
	 * Clears the hashmap.
	 */
	public void remove(){
		this.sendPacket(PersistentHashMapServer.TYPE_QUERY_FLUSH, "flush");
	}
	
	private String sendPacket(int type, String data){
		try {
			Socket s = new Socket(this.host, this.port);
			PrintWriter out = new PrintWriter(s.getOutputStream());
			out.println(type);
			out.println(data);
			out.flush();
			String buffer="";
			if(type==PersistentHashMapServer.TYPE_QUERY_GET_ID|| type==PersistentHashMapServer.TYPE_QUERY_GET_SESSION){
				BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
				buffer=read.readLine();
				read.close();
			}
			out.close();
			s.close();
			return buffer;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length<1){
			System.out.println("You must give something like: TYPE <ADD --userid, session--, GET_SESSION --user id--, GET_ID --session--, DEL_SESSION --session--, DEL_ID --user id-, FLUSH>");
			System.exit(1);
		}
		String type = args[0];
		PersistentHashMapClient c = new PersistentHashMapClient();
		switch (type) {
		case "GET_SESSION":
			System.out.println(c.getSession(new Integer(args[1])));
			break;
		case "GET_ID":
			System.out.println(c.getUserId(args[1]));
			break;
		case "ADD":
			c.add(args[2],new Integer(args[1]));
			break;
		case "DEL_SESSION":
			c.remove(args[1]);
			break;
		case "DEL_ID":
			c.remove(new Integer(args[1]));
			break;
		case "FLUSH":
			c.remove();
			break;

		default:
			break;
		}
	}
	

}
