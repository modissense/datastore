package datastore.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class ServingThread extends Thread {

	private Socket s;
	private PrintWriter out=null;
	public ServingThread(Socket s) {
		this.s=s;
		try {
			this.out= new PrintWriter(s.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public synchronized void start() {
		super.start();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String buffer=reader.readLine();
			int type=new Integer(buffer.trim());
			buffer =reader.readLine();
			switch (type) {
			case PersistentHashMapServer.TYPE_QUERY_ADD:
				PersistentHashMapServer.add(buffer.split(":")[0], new Integer(buffer.split(":")[1]));
				break;
			case PersistentHashMapServer.TYPE_QUERY_GET_SESSION:
				out.print(PersistentHashMapServer.getSession(new Integer(buffer)));
				break;
			case PersistentHashMapServer.TYPE_QUERY_GET_ID:
				out.print(PersistentHashMapServer.getId(buffer));
				break;
			case PersistentHashMapServer.TYPE_QUERY_DEL_SESSION:
				PersistentHashMapServer.remove(buffer);
				break;
			case PersistentHashMapServer.TYPE_QUERY_DEL_ID:
				PersistentHashMapServer.remove(new Integer(buffer));
				break;
			case PersistentHashMapServer.TYPE_QUERY_FLUSH:
				PersistentHashMapServer.flush();
				break;
			default:
				break;
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
