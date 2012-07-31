package kr.ac.scnu.cn.gogolib;

import java.io.*;
import java.net.*;

/**
* class to communicate with one GoGo Server embedded in GoGo Monitor
*/
public class GoGoClient 
{	
	/** {@value} */
	public static final String CMD_SET_COM = "setcom";	
	/** {@value} */
	public static final String CMD_CONNECT_BOARD = "connect";
	/** {@value}  */
	public static final String CMD_RECONNECT_BOARD = "reconnect";
	/** {@value}  */
	public static final String CMD_DISCONNECT_BOARD = "disconnect";
	/** {@value}  */
	public static final String CMD_BEEP = "beep";
	/** {@value}  */
	public static final String CMD_TALK_TO_PORT = "talktoport";
	/** {@value}  */
	public static final String CMD_SET_POWER = "setpower";
	/** {@value}  */
	public static final String CMD_MOTOR_ON = "on";
	/** {@value}  */
	public static final String CMD_MOTOR_OFF = "off";
	/** {@value}  */
	public static final String CMD_MOTOR_BREAK = "break";
	/** {@value}  */
	public static final String CMD_MOTOR_COAST = "coast";
	/** {@value}  */
	public static final String CMD_MOTOR_THISWAY = "thisway";
	/** {@value}  */
	public static final String CMD_MOTOR_THATWAY = "thatway";
	/** {@value}  */
	public static final String CMD_MOTOR_RD = "rd";
	/** {@value}  */
	public static final String CMD_LED_ON = "ledon";
	/** {@value}  */
	public static final String CMD_LED_OFF = "ledoff";
	/** {@value}  */
	public static final String CMD_SENSOR_READ = "sensor";
    
	/** it can be used to configure the Size of Command Message in bytes, default value is 512*/
	public static int maxMessageSize = 512;
	
	private PrintWriter outPrinter = null;
	private InputStream inPrinter = null;	
	private Socket clientSocket = null;
	
	/**
	* construct a GoGoClient connectted with GoGo Server    
	* @param host The IP address of GoGo Server is running
	* @param port The port number of GoGo Server
	*/
	public GoGoClient(String host, int port) throws Exception
	{
		clientSocket = new Socket(host, port);
		clientSocket.setSoTimeout(1000);
	}
	
	
	/** get the IP Address of GoGo Server
	*   @return a <em>String</em> represents IP address in <b>*.*.*.*</b> form, or <em>null</em> if it is not connected yet
	*/
	public  String getRemoteIPAddress() throws UnknownHostException
	{	
		if (clientSocket.isConnected()){
			String remote_ip=clientSocket.getInetAddress().getHostAddress();
			return remote_ip;
		}
		return null;
	}
	
	/** get the port number of GoGo Server
	*   @return an <em>integer</em> represents port number, or <em>0</em> if it is not connected yet
	*/
	public  int getRemotePort(){
	
		if (clientSocket.isConnected()){			
			int remote_port=clientSocket.getPort();
			return remote_port;
		}
		return 0;
	}
	
    /** 
	* send message to GoGo Server
	* @param cmdMessage GoGo Command Message
	*/
	public  void send(String cmdMessage) throws IOException
	{
		outPrinter = new PrintWriter(clientSocket.getOutputStream(),true);
		outPrinter.print(cmdMessage);
		outPrinter.flush();
	}

    /** 
	* receive response message from GoGo Server
	*/
	public  String receive() throws IOException
	{
		byte[] response=new byte[maxMessageSize];
        inPrinter = clientSocket.getInputStream();
		inPrinter.read(response);
		
		int c=0;
		for (c=0; c<maxMessageSize; c++){
			if (response[c]==0)
				break;
		}
		return new String(response,0,c);
	}

	/**
	* close connection
	*/
	public  void close() throws IOException
	{		
		clientSocket.close();
	}	
	
	/**
	* check connection whether it is closed
	* @return boolean if the connection is closed
	*/
	public boolean isClosed()
	{
		return clientSocket.isClosed();
	}
	
	/**
	* check connection whether it is connected	
	* @return boolean if the connection is connected
	*/
	public boolean isConnected()
	{
		return clientSocket.isConnected();
	}
	
	//----------------------
	// Built-in methods to interact with GoGo Server 
	//----------------------
	/**
	* set the COM port for remote GoGo Board
	* @param comPort port number
	*/
	public  void setCom(int comPort)throws IOException
	{
		send(CMD_SET_COM + " " + comPort);
	}
	
	/**
	* connect the remote GoGo Monitor with GoGo Board
	*/
	public  void connectBoard()throws IOException
	{
		send(CMD_CONNECT_BOARD);
	}
	
	/**
	* disconnect the remote GoGo Monitor with GoGo Board
	*/
	public  void disconnectBoard()throws IOException
	{
		send(CMD_DISCONNECT_BOARD);
	}
	
	/**
	* reconnect the remote GoGo Monitor with GoGo Board
	*/
	public  void reconnectBoard()throws IOException
	{
		send(CMD_RECONNECT_BOARD);
	}
	
	/**
	* ask the remote GoGo Board to beep
	*/
	public  void beep()throws IOException
	{
		send(CMD_BEEP);
	}
	
	/**
	* talk to the remote GoGo Board outport
	* @param port String contains character 'A', 'B', 'C' or 'D'; it can be single or combined like, "A", "B", "C", "ABC", "AD"
	*/
	public  void talkToPort(String port)throws IOException
	{
		send(CMD_TALK_TO_PORT + " " + port);
	}
	
	/**
	* set the motor power of remote GoGo Board 
	* @param power it should in period [1, 7], if it is less than 1 it will be set as 1, if it is larger than 7 it will be set as 7
	*/
	public  void setPower(int power)throws IOException
	{
		if(power < 1)
			power = 1;
		if(power > 7)
			power = 7;
		send(CMD_SET_POWER+" "+power);
	}
	
	/**
	* ask the remote GoGo Board current talking motor turn on
	*/
	public  void motorOn()throws IOException
	{
		send(CMD_MOTOR_ON);
	}
	
	/**
	* ask the remote GoGo Board current talking motor turn off
	*/
	public  void motorOff()throws IOException
	{
		send(CMD_MOTOR_OFF);
	}	
	
	/**
	* ask the remote GoGo Board current talking motor break  
	*/
	public  void motorBreak()throws IOException
	{
		send(CMD_MOTOR_BREAK);
	}
	
	/**
	* ask the remote GoGo Board current talking motor coast  
	*/
	public  void motorCoast()throws IOException
	{
		send(CMD_MOTOR_COAST);
	}
	
	/**
	* ask the remote GoGo Board current talking motor reverse direction
	*/
	public  void motorRd()throws IOException
	{
		send(CMD_MOTOR_RD);
	}
	
	/**
	* ask the remote GoGo Board current talking motor go this way 
	*/
	public  void motorThisWay()throws IOException
	{
		send(CMD_MOTOR_THISWAY);
	}
	
	/**
	* ask the remote GoGo Board current talking motor go that way 
	*/
	public  void motorThatWay()throws IOException
	{
		send(CMD_MOTOR_THATWAY);
	}	
	
	/**
	* ask the remote GoGo Board turn user led on
	*/
	public  void ledOn()throws IOException
	{
		send(CMD_LED_ON);
	}	
	
	/**
	* ask the remote GoGo Board turn user led off
	*/
	public  void ledOff()throws IOException
	{
		send(CMD_LED_OFF);
	}	
	
	/**
	* read the sensor value of remote GoGo Board
	* @param sensor sensor number it should in period [1, 8]
	*/
	public  void readSensor(int sensor)throws IOException
	{
        if(sensor < 1)
			sensor = 1;
		if(sensor > 8)
			sensor = 8;
            
        send( CMD_SENSOR_READ + sensor );
	}
	
	/**
	* a sample to use GoGo Client as a standalone application
	* @param argv argv[0] is the GoGo Server ip address,  argv[1] is port number
	*/
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