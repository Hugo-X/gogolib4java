package kr.ac.scnu.cn.gogolib;

import gnu.io.*;

import java.io.*;
import java.util.*; 
import java.text.SimpleDateFormat;

public abstract class GoGoSerialPort implements SerialPortEventListener
{
    /**
    * static scope
    */
    public static final byte CMD_HEADER1 = (byte)0x54 ;
    public static final byte CMD_HEADER2 = (byte)0xFE ;
    
    public static final byte RES_HEADER1 = (byte)0x55 ;
    public static final byte RES_HEADER2 = (byte)0xFF ;        
    public static final byte RES_ACK = (byte)0xAA ;        
	public static final byte BURST_HEADER = (byte)0x0C;
    
    // 1 byte command without parameters
	public static final byte[] CMD_PING = {(byte)0x00} ;	
	public static final byte[] CMD_MOTOR_ON = {(byte)0x40} ;
	public static final byte[] CMD_MOTOR_OFF = {(byte)0x44} ;
	public static final byte[] CMD_MOTOR_RD = {(byte)0x48} ;
	public static final byte[] CMD_MOTOR_THISWAY = {(byte)0x4C} ;
	public static final byte[] CMD_MOTOR_THATWAY = {(byte)0x50} ;
	public static final byte[] CMD_MOTOR_COAST = {(byte)0x54} ;	
    
    //2 bytes command without parameters
	public static final byte[] CMD_LED_ON = {(byte)0xC0, (byte)0x00};    
	public static final byte[] CMD_LED_OFF = {(byte)0xC1, (byte)0x00};    
	public static final byte[] CMD_BEEP = {(byte)0xC4, (byte)0x00};

	//command with parameters
    public static final byte[] CMD_SENSOR_READ = {(byte)0x20} ;
    public static final byte[] CMD_SET_POWER = {(byte)0x60} ;
	public static final byte[] CMD_TALK_TO_PORT = {(byte)0x80} ;
	public static final byte[] CMD_SET_BURST_MODE = {(byte)0xA0} ;

	// OutputPort ID for talkingToPortBits
	public static final int PORT_A = 0x01 ;
	public static final int PORT_B = 0x02 ;
	public static final int PORT_C = 0x04 ;
	public static final int PORT_D = 0x08 ;

	// Sensor ID for burstBits
	public static final int SENSOR_1 = 0x01 ;
	public static final int SENSOR_2 = 0x02 ;
	public static final int SENSOR_3 = 0x04 ;
	public static final int SENSOR_4 = 0x08 ;
	public static final int SENSOR_5 = 0x10 ;
	public static final int SENSOR_6 = 0x20 ;
	public static final int SENSOR_7 = 0x40 ;
	public static final int SENSOR_8 = 0x80 ;
    
    // SPCP response processing status
    public static final int WAITTING_FOR_NEW_CMD = 0;
    public static final int WAITTING_FOR_RES_OR_BURST_HEADER = 1;
    public static final int WAITTING_FOR_RES_HEADER2  = 2;
    public static final int WAITTING_FOR_RES_BYTE1    = 3;
    public static final int WAITTING_FOR_RES_BYTE2    = 4;
    public static final int WAITTING_FOR_RES_BYTE3    = 5;
    public static final int WAITTING_FOR_RES_BYTE4    = 6;
    public static final int WAITTING_FOR_BURST_BYTE1  = 7;
    public static final int WAITTING_FOR_BURST_BYTE2  = 8;
    public static final int CMD_RES_READY  = 9;
    public static final int BURST_RES_READY  = 10;
    
    
    // response bytes type
    public static final int RES_BYTES_1 = 1;
    public static final int RES_BYTES_2 = 2;
    public static final int RES_BYTES_4 = 4;
    
    // debug utility
    public static PrintStream debugOut;
    //**************************************************************    
    
    /**
    * connection scope
    */
    //protected PushbackInputStream inputStream;
    protected InputStream inputStream;
    protected OutputStream outputStream ;   
    
    private int GoGoSPCPStatus;
	private int GoGoWaittingForDataSize;    
	private byte burstBits, talkingToPortBits;
    //protected ResponseBuffer cmdResponse, burstResponse;
    private byte[] cmdResponseBytes, burstResponseBytes;
    private Vector<Byte> serailInputBuffer;
    private String portName;
    private SerialPort port;
	private ResponseProcessThread innerResponseProcessThread;
	private GetCommandResponseThread innerCommandResponseThread;
	private GetBurstResponseThread innerBurstResponseThread;
	
	private boolean[] cmdWaitingforReply;
    private boolean[] burstOn;
    

    /**
    * constructor, init Serial Port Communication Protocol status
    */
    public GoGoSerialPort(){
        GoGoSPCPStatus = 0;
        GoGoWaittingForDataSize = 0;
        
        //cmdResponseBytes = null;
        cmdResponseBytes = new byte[4];
        burstResponseBytes = new byte[2];
        
        burstBits = (byte)0x00;
        talkingToPortBits = (byte)0x00;
        portName = null;
        port = null;
        serailInputBuffer = new Vector<Byte>(1024);
        //cmdResponse = new ResponseBuffer(maxCmdResponseSize);
        //burstResponse = new ResponseBuffer(burstResponseSize);
        cmdWaitingforReply = new boolean[1];
        cmdWaitingforReply[0] = false;
        burstOn = new boolean[1];
        burstOn[0] = true;
    }
        
    
    
    //---------------utilities---------------------    
    static final String HEXES = "0123456789ABCDEF";    
    public static String getHexString( Byte[] byteA ) {
        if ( byteA == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * byteA.length );
        for ( final byte b : byteA ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
            .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
    public static String getHexString( byte[] byteA ) {
        if ( byteA == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * byteA.length );
        for ( final byte b : byteA ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
            .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
    //----------------------------------------------
    
    /** send command to serial port */ 
    public void sendCommand(byte []cmdBytes){
        try{
            outputStream.write(new byte[]{CMD_HEADER1,CMD_HEADER2});
            outputStream.write(cmdBytes);
            outputStream.flush();
        }catch(IOException ex){
            String cmd = getHexString(cmdBytes);
            String timeStr = new String (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date()));
            System.err.println("Failed to send command: " + cmd + " @ " + timeStr);
        }
        
        if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entering cmdWaitingforReply");
        synchronized(cmdWaitingforReply){
        	try{
        		if(cmdWaitingforReply[0]){
        			cmdWaitingforReply.wait();
        			if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" waiting for cmdWaitingforReply to be false");
        		}
        	}catch(InterruptedException e) {
				e.printStackTrace();	
        	}
        	
            cmdWaitingforReply[0] = true;
            cmdWaitingforReply.notify();
            
            if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" leaving cmdWaitingforReply, cmdWaitingforReply is true now");
        }
    }   
  
    
    /** read serial input*/
    public void getBytesFromSerialPort(){
    	byte[] tmpBuffer = new byte[1024];
    	int n = 0;
    	try {
			n = inputStream.read(tmpBuffer);

			if(debugOut != null){
				byte[] tmp = new byte[n];
				for(int i=0; i<n; i++)
					tmp[i]=tmpBuffer[i];
				debugOut.println("get data:"+getHexString(tmp)+","+n+" bytes");				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			//String timeStr = new String (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date()));
			//System.err.println(" Failed to read a byte from serial port " + portName + " @ " + timeStr);
		}
		if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entering serailInputBuffer");
		synchronized(serailInputBuffer){
			if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entered serailInputBuffer");
			// append the new data to serailInputBuffer
			for(int i=0; i<n; i++){
				serailInputBuffer.add(tmpBuffer[i]);
			}
			
			// if the buffer is not empty notify the SPCPThread to start
			if(!serailInputBuffer.isEmpty()){
				
				serailInputBuffer.notify();
			}
			if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" leaving serailInputBuffer");
		}
    }
    
    /** method of SerialPortEventListener*/    
    public void serialEvent(SerialPortEvent event){
        if(event.getEventType()==SerialPortEvent.DATA_AVAILABLE) {
        	getBytesFromSerialPort();
        }
    }   

    
    
    /** start the serial port communication */
    public void startSPCP(){
    	
    	GoGoSPCPStatus = GoGoSerialPort.WAITTING_FOR_RES_OR_BURST_HEADER;
    	
    	innerResponseProcessThread = new ResponseProcessThread();
    	innerResponseProcessThread.setName("innerResponseProcessThread");
    	innerResponseProcessThread.start();
    	
    	innerCommandResponseThread = new GetCommandResponseThread();
    	innerCommandResponseThread.setName("innerCommandResponseThread");
    	innerCommandResponseThread.start();
    	
        innerBurstResponseThread = new GetBurstResponseThread();
        innerBurstResponseThread.setName("innerBurstResponseThread");
        innerBurstResponseThread.start();
    }
    
    /** stop the serial port communication */
    public void stopSPCP() {    	
        
        while(innerBurstResponseThread.isAlive()){
        	if (debugOut!=null) debugOut.println("to interrupt innerBurstResponseThread");
            innerBurstResponseThread.interrupt();
        }
    	
        if (debugOut!=null) debugOut.println("innerBurstResponseThread died");
        
    	while(innerCommandResponseThread.isAlive()){
    		if (debugOut!=null) debugOut.println("to interrupt innerCommandResponseThread");
            innerCommandResponseThread.interrupt();
        }
    	
    	if (debugOut!=null) debugOut.println("innerCommandResponseThread died");
    	 
    	while(innerResponseProcessThread.isAlive()){
    		if (debugOut!=null) debugOut.println("to interrupt innerResponseProcessThread");
    		innerResponseProcessThread.interrupt();           
        }
    	if (debugOut!=null) debugOut.println("innerResponseProcessThread died");
	}    
    
    /** inner ResponseProcessThread to handle the serial port input data and communication status*/
    public class ResponseProcessThread extends Thread{
    	byte inByte = 0x00;
		//boolean inByteUpdated = false;
		
    	public void run(){
			
    		while(!isInterrupted()){
    			if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" is running...");
    			try{
	    			// read the first byte from serial port input buffer
	    			if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entering serailInputBuffer");
	    			synchronized(serailInputBuffer){
	    				if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entered serailInputBuffer");
	    	    		if(serailInputBuffer.isEmpty()){
	    	    			if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" is waiting for serailInputBuffer...");
							serailInputBuffer.wait();
	    	    		}
	    	    		inByte = serailInputBuffer.remove(0);
	    	    		if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" leaving serailInputBuffer");
	    			}
	    			    			
	    	        SPCPStatusRoutine(inByte);
	    	        
    			}catch (InterruptedException e) {
    				//e.printStackTrace();
    				break;
    			}
    		}//end of while
    	}//end of run
    }
    
    /** Communication status routine */
    private void SPCPStatusRoutine(byte inByte){
    	switch(GoGoSPCPStatus){
            case WAITTING_FOR_RES_OR_BURST_HEADER: // WAITTING_FOR_RES_OR_BURST_HEADER
            	if(debugOut!=null) debugOut.println("WAITTING_FOR_RES_OR_BURST_HEADER");
                if ( inByte == RES_HEADER1 ) 
                    GoGoSPCPStatus = WAITTING_FOR_RES_HEADER2;                     
                else if ( inByte == BURST_HEADER ) 
                    GoGoSPCPStatus = WAITTING_FOR_BURST_BYTE1; 
                else
                    ;// keep current status
                break;
                
                
            case WAITTING_FOR_RES_HEADER2: // WAITTING_FOR_RES_HEADER2
            	if(debugOut!=null) debugOut.println("WAITTING_FOR_RES_HEADER2");
                if ( inByte == RES_HEADER2 )
                    GoGoSPCPStatus = WAITTING_FOR_RES_BYTE1;
                else
                    ;//keep current status
                break;
                
                
            case WAITTING_FOR_RES_BYTE1: // process the response data bytes   
            	if(debugOut!=null) debugOut.println("WAITTING_FOR_RES_BYTE1");
                synchronized (cmdResponseBytes){
                    if(GoGoWaittingForDataSize == RES_BYTES_1)
                    {
                        GoGoSPCPStatus = CMD_RES_READY;
                        cmdResponseBytes[0] = inByte;
                        cmdResponseBytes.notify();
                    }else{
                        GoGoSPCPStatus = WAITTING_FOR_RES_BYTE2;
                        cmdResponseBytes[0] = inByte;
                        break;
                    }
                }
                break;
                
            case WAITTING_FOR_RES_BYTE2:   
            	if(debugOut!=null) debugOut.println("WAITTING_FOR_RES_BYTE2");
                synchronized (cmdResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_RES_BYTE3;
                    cmdResponseBytes[1] = inByte;
                    if (GoGoWaittingForDataSize == RES_BYTES_2) {
                        GoGoSPCPStatus = CMD_RES_READY;// ready for 2 bytes
                        cmdResponseBytes.notify();
                    }
                }
                break;
            
            case WAITTING_FOR_RES_BYTE3:      
            	if(debugOut!=null) debugOut.println("WAITTING_FOR_RES_BYTE3");
                synchronized (cmdResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_RES_BYTE4;
                    cmdResponseBytes[2] = inByte;
                }
                break;
                
            case WAITTING_FOR_RES_BYTE4:       
            	if(debugOut!=null) debugOut.println("WAITTING_FOR_RES_BYTE4");
                synchronized (cmdResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_RES_BYTE2;
                    cmdResponseBytes[3] = inByte;
                    GoGoSPCPStatus = CMD_RES_READY;
                    cmdResponseBytes.notify();
                }                    
                break;
            
            case WAITTING_FOR_BURST_BYTE1:  // burst byte1       
            	if(debugOut!=null) debugOut.println("WAITTING_FOR_BURST_BYTE1");
                synchronized (burstResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_BURST_BYTE2;
                    burstResponseBytes[0] = inByte;
                }
                break;
                
            case WAITTING_FOR_BURST_BYTE2:  // burst byte2  
            	if(debugOut!=null) debugOut.println("WAITTING_FOR_BURST_BYTE2");
                synchronized (burstResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_RES_OR_BURST_HEADER;
                    burstResponseBytes[1] = inByte;
                    GoGoSPCPStatus = BURST_RES_READY;
                    burstResponseBytes.notify();
                }
                break;
            
            case CMD_RES_READY:
            	if(debugOut!=null) debugOut.println("CMD_RES_READY");
                if ( inByte == BURST_HEADER ) 
                    GoGoSPCPStatus = WAITTING_FOR_BURST_BYTE1;
                else 
                    ; //keep current status
                break;
                
            default: 
            	if(debugOut!=null) debugOut.println("Unknown GoGoSPCPStatus");
                GoGoSPCPStatus = WAITTING_FOR_RES_OR_BURST_HEADER;                
        }//end of switch
    }
    
    /** inner class of GetCommandResponseThread */
    public class GetCommandResponseThread extends Thread{
        byte[] responseBytes;
        
        public void run(){
            while(!isInterrupted()){
            	if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" is running...");
                try{
                	if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entering cmdWaitingforReply");
                    synchronized(cmdWaitingforReply){
                    	if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entered cmdWaitingforReply");
                        // do not need to get command response until cmdWaitingforReply[0] is true
                        if(!cmdWaitingforReply[0]){
                            if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" is waiting for cmdWaitingforReply to be true");
                            cmdWaitingforReply.wait();
                        }
                        
                        // get command response
                        if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entering cmdResponseBytes");
                        synchronized (cmdResponseBytes){
                        	if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" entered cmdResponseBytes");
	                        if(GoGoSPCPStatus != CMD_RES_READY){
	                        	if(debugOut != null) debugOut.println(Thread.currentThread().getName()+" is waiting for cmdResponseBytes");
	                        	cmdResponseBytes.wait();	                            
	                        }
	                        responseBytes = new byte[GoGoWaittingForDataSize];
	                        for(int i = 0; i < GoGoWaittingForDataSize; i++)
	                        	responseBytes[i] = cmdResponseBytes[i];
	                        
	                        if(debugOut != null) debugOut.println(Thread.currentThread().getName()+"leaving cmdResponseBytes");
                        }
                        
                        //handle command response
                        cmdResponseHandle(responseBytes);
                        if(debugOut != null) debugOut.println(getHexString(responseBytes));
                        
                        cmdWaitingforReply[0] = false;
                        cmdWaitingforReply.notify();
                        
                        if(debugOut != null) debugOut.println(Thread.currentThread().getName()+"leaving cmdWaitingforReply");
                    }
                    
                }catch(InterruptedException ex){
                    //ex.printStackTrace();                    
                    break;
                }
            }
        }
    }
    
    /** inner class of GetBurstResponseThread */
    public class GetBurstResponseThread extends Thread{
    	
    	byte []responseBytes;

        public void run(){        	
            while(!isInterrupted()){
            	if(debugOut != null) debugOut.println("GetBurstResponseThread is running...");
                try{
                	if(debugOut != null) debugOut.println("GetBurstResponseThread entering burstOn...");
                    synchronized(burstOn){
                    	if(debugOut != null) debugOut.println("GetBurstResponseThread entered burstOn...");
                    	// do not need to get burst response until burstOn[0] is true
                    	if(!burstOn[0]){
                        	if(debugOut != null) debugOut.println("GetBurstResponseThread is waiting for burstOn...");
                            burstOn.wait();
                        }
                    	
                    	if(debugOut != null) debugOut.println("GetBurstResponseThread entering burstResponseBytes...");
                    	synchronized (burstResponseBytes){
                    		if(debugOut != null) debugOut.println("GetBurstResponseThread entered burstResponseBytes...");
                            
                    		if(GoGoSPCPStatus != BURST_RES_READY){
                            	if(debugOut != null) debugOut.println("GetBurstResponseThread is waiting for burstResponseBytes...");
                            	burstResponseBytes.wait();                                
                            }
                            responseBytes = burstResponseBytes;
                            
                            if(debugOut != null) debugOut.println("GetBurstResponseThread leaving burstResponseBytes...");
                        }
                    	
                        if(debugOut != null) debugOut.println("burst: "+getHexString(responseBytes));
                        
                        burstResponseHandle(responseBytes);
                        
                        
                    	if(debugOut != null) debugOut.println("GetBurstResponseThread leaving burstOn...");
                    }
                }catch(InterruptedException ex){
                    //ex.printStackTrace();                    
                    break;
                }
            }
        }
    }
    
    /** display or print the command response*/
    abstract public void cmdResponseHandle(byte[] responseBytes);
    /** display or print the burst response*/
    abstract public void burstResponseHandle(byte[] responseBytes);
    
    /** peek the first byte, and don't remove it from the stream*/
    /*public byte peekByte() throws IOException{ 
        int b;
        try{
            b = inputStream.read();
            inputStream.unread(b);
        } catch (IOException e){
            e.printStackTrace();
        }
		return (byte) b;
    } */    
    
    //********************************************************************* 
    
    /** set the serial port number for GoGo Board */
    public void setPort(String portStr) {
        portName = portStr;
    }
    
    /** */
    @SuppressWarnings("unchecked")
	public static Vector<String> listSerialPortNames(){
    	Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();		
		CommPortIdentifier tmpPortId = null;
		
		Vector<String> portNames = new Vector<String>(10) ;		
		
		while (portIdentifiers.hasMoreElements()) {
			tmpPortId = portIdentifiers.nextElement();
			if ( tmpPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)			
				portNames.add( tmpPortId.getName() ) ;
		}
		
		return portNames;
    }
    
    /** connect with GoGo Board */
	@SuppressWarnings("unchecked")
	public void	connectBoard(String appName, int acquireTimeOut, int receiveTimeOut) throws Exception{
    
        // find port by name		
        Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();		
		CommPortIdentifier portId = null, tmpPortId = null;
        
		while (portIdentifiers.hasMoreElements()) {
			tmpPortId = portIdentifiers.nextElement();
            
			if (tmpPortId.getName().equals(portName) && 
                    tmpPortId.getPortType() == CommPortIdentifier.PORT_SERIAL){
					portId = tmpPortId;
                    break;
			}
		}
        
        if (portId == null){
            throw new RuntimeException("Unable to detect serial port: " + portName) ;
        }
        
        // open port
        try {
            port = (SerialPort) tmpPortId.open(appName, acquireTimeOut) ;
        }
        catch (PortInUseException e)
        {
            throw new Exception( portName + " is already in use!") ;            
        }
        catch ( RuntimeException e )
        {
            throw new Exception("Unable to open serial port: " + portName) ;
        }
        
        // set parameters
        try {
            port.setSerialPortParams(9600,
                                     SerialPort.DATABITS_8,
                                     SerialPort.STOPBITS_1,
                                     SerialPort.PARITY_NONE);
            // set receiveTimeOut
            port.enableReceiveTimeout(receiveTimeOut);
            
        } catch (UnsupportedCommOperationException e) {
            //e.printStackTrace();
            throw new Exception("Unable to open serial port: " + portName) ;
        }
        
        // initialize I/O stream
        try {
        	inputStream = port.getInputStream();
            outputStream = port.getOutputStream();
        } catch (IOException e) {
            throw new Exception("Unable to initialize the I/O stream.");
        }
        
        // addEventListerner
        try{
            port.addEventListener(this);
        }catch(TooManyListenersException ex){
            ex.printStackTrace();
        }
        port.notifyOnDataAvailable(true);
    }
    
    /** disconnect with GoGo Board */ 
    public void disconnectBoard() {
        if (port != null) {
            port.removeEventListener();
            try {
                if (inputStream != null) 
                    inputStream.close();
                if (outputStream != null)                
                    outputStream.close();	
            }catch (IOException e) {  
                e.printStackTrace();
            }            
            inputStream = null;
            outputStream = null;
            port.close();
            port = null;
        }
    }
    
    //*************************************************************************
    /** ping GoGo Board */
    public void ping(){
        sendCommand(CMD_PING);
        GoGoWaittingForDataSize = RES_BYTES_4;
    } 
    
    /** ask GoGo Board beep */
    public void beep(){
        sendCommand(CMD_BEEP);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }    
    
    /** ask GoGo Board turn user led off */
    public void ledOff(){
        sendCommand(CMD_LED_OFF);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }  
    
    /** ask GoGo Board turn user led on*/
    public void ledOn() {
        sendCommand(CMD_LED_ON);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }
    
    /**ask GoGo Board current talking motor coast    */
    public void motorCoast() {
        sendCommand(CMD_MOTOR_COAST);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }
    
    /** ask GoGo Board current talking motor turn off  */
    public void motorOff() {
        sendCommand(CMD_MOTOR_OFF);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }
    
    /** ask GoGo Board current talking motor turn on */
    public void motorOn() {
        sendCommand(CMD_MOTOR_ON);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }
    
    /** ask  GoGo Board current talking motor reverse direction*/
    public void motorRd() {
        sendCommand(CMD_MOTOR_RD);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }
    
    /** ask GoGo Board current talking motor go that way */
    public void motorThatWay() {
        sendCommand(CMD_MOTOR_THATWAY);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }
    
    /** ask GoGo Board current talking motor go this way */
    public void motorThisWay() {
        sendCommand(CMD_MOTOR_THISWAY);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }

    /** read the sensor value of GoGo Board */
    public void readSensor(int sensor) {
        byte[] cmd = new byte[1];
        cmd[0] = CMD_SENSOR_READ[0];
        cmd[0] |= (byte)(sensor<<2);
        sendCommand(cmd);
        GoGoWaittingForDataSize = RES_BYTES_2;
    }    
    
    /** set the motor power of GoGo Board */
    public void setPower(int power) {
        byte[] cmd = new byte[1];
        cmd[0] = CMD_SET_POWER[0];
        cmd[0] += (byte)power;
        sendCommand(cmd);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }

    /** set the talking to outports of GoGo Board */
    public void talkTo(int port) {
        talkingToPortBits = (byte)port;
        byte[] cmd = new byte[2];
        cmd[0] = CMD_TALK_TO_PORT[0];
        cmd[1] = (byte)talkingToPortBits;
        sendCommand(cmd);
        GoGoWaittingForDataSize = RES_BYTES_1;
    }
    
    //** turn on or turn off the burst mode of specified sensors */
    public void setBurst(int sensorBits){
        burstBits = (byte)(sensorBits & 0xFF);
        byte[] cmd = new byte[2];
        
        //here, the burst speed is not took into account
        cmd[0] = CMD_SET_BURST_MODE[0];
        
        cmd[1] = burstBits;
        sendCommand(cmd);
        
        //System.out.println("burstBits-set-cmd = " + getHexString(cmd));
        
        GoGoWaittingForDataSize = RES_BYTES_1;
        
        // control GetBurstResponseThread 
        synchronized(burstOn){
        	boolean preBurstOn = burstOn[0];
            if(sensorBits > 0){
                burstOn[0] = true;
                if(!preBurstOn)
                	burstOn.notify();
            }else{
                burstOn[0] = false;
            }
        }
    }	
    
    /**
    * Response Buffer
    *//*
    public class ResponseBuffer{
        protected byte[] dataBytes;
        protected int dataLength, requiredDataLength;
        
        public ResponseBuffer(int bufferSize){
            dataBytes = new byte[bufferSize];
            dataLength = 0;
            requiredDataLength = 0;
        }        
        
        public boolean dataIsReady(){
            if( dataLength ==  requiredDataLength)
                return true;
            else 
                return false;
        }        
        
        public byte[] getResponseData(){
            if(!dataIsReady())
                wait();
        }
        
        public void addByte(byte dataByte) {
            dataBytes[dataLength] = dataByte;
            dataLength ++;
            if(dataIsReady())
                notify();
        }
        
        public void setRequirement(int requiredLength){
            requiredDataLength = requiredLength;
        }
    }*/
}

