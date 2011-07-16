package kr.ac.scnu.cn.gogolib;

import java.io.*;
import java.util.*;

/** class to manage multiple connections to GoGo Server*/
public class GoGoSessionManager
{		
    private static HashMap<Integer,GoGoClient> clientPool=new HashMap<Integer,GoGoClient>(3);
	private static int id=0;
	
	/** create a new connection to GoGo server at <em>host:port</em>, return connection <em>id</em> or <em>0</em> if it is failed */    
	public static int connect(String host, int port) throws Exception
	{
		GoGoClient tmpClient = new GoGoClient(host, port);
		
		if (tmpClient !=null)
		{
			id++;
			clientPool.put(id, tmpClient);
			return id;
		}
		return 0;
	} 
	
	/** get a GoGoClient instance by connection id <em>i</em> */
	public static GoGoClient getClient(int i)
	{
		return clientPool.get(i);
	}
	
    /** send message through connection <em>i</em> */
	public static void send(int i, String message) throws IOException
	{	
		if(!clientPool.containsKey(i))
			return;
			
		clientPool.get(i).send(message);
	}

    /** receive message through connection <em>i</em> */
	public static String recv(int i) throws IOException
	{
		if(!clientPool.containsKey(i))
			return null;
			
		return clientPool.get(i).receive();		
	}

    /** close connection <em>i</em> with server */    
	public static void disconnect(int i) throws IOException
	{
		if(!clientPool.containsKey(i))
			return;
			
		clientPool.get(i).close();
        clientPool.remove(i);
	}

    /** disconnect all the connections */    
    public static void disconnectAll() throws IOException
	{
		if(clientPool.isEmpty())
			return;
			
		Iterator it=clientPool.values().iterator();
		
		while(it.hasNext()){
			GoGoClient client=(GoGoClient)it.next();
			client.close();
		}		
		clientPool.clear();
	}	   
}