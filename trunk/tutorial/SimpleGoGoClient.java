/*
* Put the gogolib.jar beside this source code
* Compile SimpleGoGoClient.java by command -> javac -cp gogolib.jar SimpleGoGoClient.java
*/
import kr.ac.scnu.cn.gogolib.GoGoClient;
import java.io.*;

public class SimpleGoGoClient
{	
	public static void main(String []argv) throws Exception
	{
		String strToServer, strFromServer;
		String	host;
		int port;
		GoGoClient ggClient;
		
		// default connection
		host="localhost";
		port=9873;
		
		if(argv.length>0){
			if(argv.length!=2){
				System.out.println("Incorrect arguments.");
				System.out.println("Usage: java -jar gogoClient.jar ip port | no arguments will connect to localhost:9873");
				return;
			}
			host=argv[0];
			port=Integer.parseInt(argv[1]);
		}
		
		ggClient = new GoGoClient(host, port);		
		
		if(ggClient == null)
			return;
			
		System.out.print("Connected with GoGo Server@"+host+":"+port+"(Type Q or q to Quit)\n>");
		
		BufferedReader inFromUser =	 new BufferedReader(new InputStreamReader(System.in));
		strToServer = inFromUser.readLine();
		
		while (!(strToServer.equals("Q") || strToServer.equals("q")))
		{		 
			//send
			System.out.println(">>>>>Sending "+strToServer+"...");		
			ggClient.send(strToServer);

			//recv
			strFromServer = ggClient.receive();
			System.out.println("<"+strFromServer);

			//next
			System.out.print(">");
			strToServer = inFromUser.readLine();
		}
	
	} 
}