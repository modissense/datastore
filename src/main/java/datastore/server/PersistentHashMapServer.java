package datastore.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;


public class PersistentHashMapServer {
	
	public static final int PORT=4545;
	protected static int TRANSACTIONS_FOR_LOGGING=10;
	
	public static final int 		TYPE_QUERY_ADD=0,
									TYPE_QUERY_GET_SESSION=1,
									TYPE_QUERY_GET_ID=2,
									TYPE_QUERY_DEL_SESSION=3,
									TYPE_QUERY_DEL_ID=4,
									TYPE_QUERY_FLUSH=5;
	
	protected static HashMap<String, Integer> sessionKey = new HashMap<String,Integer>();
	private static HashMap<Integer, String> idKey = new HashMap<Integer,String>();
	
	private static Object lock = new Object();
	public PersistentHashMapServer() {
		sessionKey = new HashMap<String, Integer>();
		idKey = new HashMap<Integer, String>();
	}
	
	protected static void add(String session, Integer key){
		if(PersistentHashMapServer.sessionKey.size()%PersistentHashMapServer.TRANSACTIONS_FOR_LOGGING==0){
			System.out.format("%s\tADDING(%s,%d)\tSIZE:%d\n",new Date(),session,key,sessionKey.size());
		}
		synchronized (lock) {
			sessionKey.put(session, key);
			idKey.put(key, session);
		}
	}
	
	protected static int getId(String session){
		if(PersistentHashMapServer.sessionKey.size()%PersistentHashMapServer.TRANSACTIONS_FOR_LOGGING==0){
			System.out.format("%s\tGET ID(%s)\tSIZE:%d\n",new Date(),session,sessionKey.size());
		}
		return (sessionKey.get(session)!=null?sessionKey.get(session):-1);
	}
	
	protected static String getSession(Integer id){
		if(PersistentHashMapServer.sessionKey.size()%PersistentHashMapServer.TRANSACTIONS_FOR_LOGGING==0){
			System.out.format("%s\tGET SESSION(%d)\tSIZE:%d\n",new Date(),id,sessionKey.size());
		}

		return (idKey.get(id)!=null?idKey.get(id):"null");
	}
	
	protected static void remove(String session){
		if(PersistentHashMapServer.sessionKey.size()%PersistentHashMapServer.TRANSACTIONS_FOR_LOGGING==0){
			System.out.format("%s\tREMOVE BY SESSION(%s)\tSIZE:%d\n",new Date(),session,sessionKey.size());
		}
		
		synchronized (lock) {
			if(session!=null){
				if(!sessionKey.containsKey(session))
					return;
				int temp = sessionKey.get(session);
				idKey.remove(temp);
				sessionKey.remove(session);
			}
		}
	}
	
	protected static void remove(Integer id){
		if(PersistentHashMapServer.sessionKey.size()%PersistentHashMapServer.TRANSACTIONS_FOR_LOGGING==0){
			System.out.format("%s\tREMOVE BY ID(%d)\tSIZE:%d\n",new Date(),id,sessionKey.size());
		}
		synchronized (lock) {
			String temp = idKey.get(id);
			idKey.remove(id);
			if(temp!=null){
				sessionKey.remove(temp);
			}
		}
	}
	
	protected static void flush(){
		if(PersistentHashMapServer.sessionKey.size()%PersistentHashMapServer.TRANSACTIONS_FOR_LOGGING==0){
			System.out.format("%s\tFLUSH\tSIZE:%d\n",new Date(),sessionKey.size());
		}
		synchronized (lock) {
			sessionKey.clear();
			idKey.clear();
		}
	}
	
	protected static void serve(Socket s) throws IOException{
		ServingThread foo = new ServingThread(s);
		foo.start();
		s.close();
	}
	
	public static void main(String[] args) {
		if(args.length>0){
			PersistentHashMapServer.TRANSACTIONS_FOR_LOGGING=new Integer(args[0]);
		}
		ServerSocket sock;
		while(true){
			try {
				 sock = new ServerSocket(PersistentHashMapServer.PORT);
				while(true){
					Socket s =sock.accept(); 
					serve(s);
					if(sock.isClosed()){
						System.err.format("%s\t[ERROR] CONNENCTION IS CLOSED -- REOPENING\n",new Date());
						sock = new ServerSocket(PersistentHashMapServer.PORT);
					}
				}
			} catch (IOException e) {
				System.err.format("%s\t[ERROR] IO EXCEPTION\n",new Date());
				e.printStackTrace();
			} catch (Exception a) {
				System.err.format("%s\t[ERROR] GENERAL EXCEPTION\n",new Date());
				a.printStackTrace();
			}
		}
	}
}

