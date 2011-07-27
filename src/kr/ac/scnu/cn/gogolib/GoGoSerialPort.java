package kr.ac.scnu.cn.gogolib;

import gnu.io.*;

import java.io.*;
import java.util.*; 
import java.text.SimpleDateFormat;

public class GoGoSerialPort implements SerialPortEventListener
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
    
    
    //**************************************************************    
    
    /**
    * connection scope
    */
    //protected PushbackInputStream inputStream;
    protected InputStream inputStream;
    protected OutputStream outputStream ;   
    
    public int GoGoSPCPStatus, GoGoWaittingForDataSize;    
	protected byte burstBits, talkingToPortBits;
    //protected ResponseBuffer cmdResponse, burstResponse;
    public byte[] cmdResponseBytes, burstResponseBytes;
    public String portName;
    public SerialPort port;

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
        //cmdResponse = new ResponseBuffer(maxCmdResponseSize);
        //burstResponse = new ResponseBuffer(burstResponseSize);
    }
    
    
    public void processResponse(){               
        
        byte inByte = readByte();
        
        switch(GoGoSPCPStatus)
        {
            case WAITTING_FOR_RES_OR_BURST_HEADER: // WAITTING_FOR_RES_OR_BURST_HEADER
                if ( inByte == RES_HEADER1 ) 
                    GoGoSPCPStatus = WAITTING_FOR_RES_HEADER2;                     
                else if ( inByte == BURST_HEADER ) 
                    GoGoSPCPStatus = WAITTING_FOR_BURST_BYTE1; 
                else
                    ;// keep current status
                break;
                
                
            case WAITTING_FOR_RES_HEADER2: // WAITTING_FOR_RES_HEADER2
                if ( inByte == RES_HEADER2 )
                    GoGoSPCPStatus = WAITTING_FOR_RES_BYTE1;
                else
                    ;//keep current status
                break;
                
                
            case WAITTING_FOR_RES_BYTE1: // process the response data bytes                
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
                synchronized (cmdResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_RES_BYTE4;
                    cmdResponseBytes[2] = inByte;
                }
                break;
                
            case WAITTING_FOR_RES_BYTE4:                
                synchronized (cmdResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_RES_BYTE2;
                    cmdResponseBytes[3] = inByte;
                    GoGoSPCPStatus = CMD_RES_READY;
                    cmdResponseBytes.notify();
                }                    
                break;
            
            case WAITTING_FOR_BURST_BYTE1:  // burst byte1                        
                synchronized (burstResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_BURST_BYTE2;
                    burstResponseBytes[0] = inByte;
                }
                break;
                
            case WAITTING_FOR_BURST_BYTE2:  // burst byte2                         
                synchronized (burstResponseBytes){
                    GoGoSPCPStatus = WAITTING_FOR_RES_OR_BURST_HEADER;
                    burstResponseBytes[1] = inByte;
                    GoGoSPCPStatus = BURST_RES_READY;
                    burstResponseBytes.notify();
                }
                break;
            
            case CMD_RES_READY:
                if ( inByte == BURST_HEADER ) 
                    GoGoSPCPStatus = WAITTING_FOR_BURST_BYTE1;
                else 
                    ; //keep current status
                
            default: 
                GoGoSPCPStatus = WAITTING_FOR_RES_OR_BURST_HEADER;                
        }   

    }   
    
    public byte[] getBurstResponse(){
        synchronized (burstResponseBytes){
            try{
                if(GoGoSPCPStatus != BURST_RES_READY)
                    burstResponseBytes.wait();
            }catch(InterruptedException ex){
                ex.printStackTrace();
            }
            return burstResponseBytes;
        }
    }
    
    public byte[] getCommandResponse(){
        synchronized (cmdResponseBytes){
            try{
                if(GoGoSPCPStatus != CMD_RES_READY)
                    cmdResponseBytes.wait();
            }catch(InterruptedException ex){
                ex.printStackTrace();
            }
            byte[] dataBytes = new byte[GoGoWaittingForDataSize];
            for(int i = 0; i < GoGoWaittingForDataSize; i++)
                dataBytes[i] = cmdResponseBytes[i];
            return dataBytes;
        }        
    }    
    
    //---------------utilities---------------------    
    static final String HEXES = "0123456789ABCDEF";    
    public static String getHexString( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
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
    } 
    
    
    /** read the first byte, and remove it from the stream */
    public byte readByte(){
        try {
            return (byte)inputStream.read();
		} catch (IOException e) {            
            String timeStr = new String (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date()));
			System.err.println(" Failed to read a byte from serial port " + portName + " @ " + timeStr);
            return 0x00;
		}
    }
    
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
    
    // initialize the inputstream
    private void initInputStream() throws IOException{        
            inputStream = port.getInputStream();
    }
    
    // initialize the outputstream
    private void initOutputStream() throws IOException{
            outputStream = port.getOutputStream();
    }   
    
    /** set the serial port number for GoGo Board */
    public void setPort(String portStr) {
        portName = portStr;
    }
    
    /** connect with GoGo Board */
	@SuppressWarnings("unchecked")
	public void	connectBoard(String appName, int acquireTimeOut, int receiveTimeOut) throws Exception{
    
        // find port by name
        Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();
		
		CommPortIdentifier tmpPortId = null;
        
		while (portIdentifiers.hasMoreElements()) {
			tmpPortId = portIdentifiers.nextElement();
            
			if (tmpPortId.getName().equals(portName) && 
                    tmpPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)     {
                    break;
			}
		}
        
        if (tmpPortId == null){
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
            port.enableReceiveTimeout(receiveTimeOut);
        } catch (UnsupportedCommOperationException e) {
            //e.printStackTrace();
            throw new Exception("Unable to open serial port: " + portName) ;
        }
        
        // initialize I/O stream
        try {
            initInputStream();
            initOutputStream();
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
    } 
    
    /** ask GoGo Board beep */
    public void beep(){
        sendCommand(CMD_BEEP);
    }    
    
    /** ask GoGo Board turn user led off */
    public void ledOff(){
        sendCommand(CMD_LED_OFF);
    }  
    
    /** ask GoGo Board turn user led on*/
    public void ledOn() {
        sendCommand(CMD_LED_ON);
    }
    
    /**ask GoGo Board current talking motor coast    */
    public void motorCoast() {
        sendCommand(CMD_MOTOR_COAST);
    }
    
    /** ask GoGo Board current talking motor turn off  */
    public void motorOff() {
        sendCommand(CMD_MOTOR_OFF);
    }
    
    /** ask GoGo Board current talking motor turn on */
    public void motorOn() {
        sendCommand(CMD_MOTOR_ON);
    }
    
    /** ask  GoGo Board current talking motor reverse direction*/
    public void motorRd() {
        sendCommand(CMD_MOTOR_RD);
    }
    
    /** ask GoGo Board current talking motor go that way */
    public void motorThatWay() {
        sendCommand(CMD_MOTOR_THATWAY);
    }
    
    /** ask GoGo Board current talking motor go this way */
    public void motorThisWay() {
        sendCommand(CMD_MOTOR_THISWAY);
    }

    /** read the sensor value of GoGo Board */
    public void readSensor(int sensor) {
        byte[] cmd = new byte[1];
        cmd[0] = CMD_SENSOR_READ[0];
        cmd[0] |= (byte)(sensor<<2);
        sendCommand(cmd);
    }    
    
    /** set the motor power of GoGo Board */
    public void setPower(int power) {
        byte[] cmd = new byte[1];
        cmd[0] = CMD_SET_POWER[0];
        cmd[0] += (byte)power;
        sendCommand(cmd);
    }

    /** set the talking to outports of GoGo Board */
    public void talkTo(int port) {
        talkingToPortBits = (byte)port;
        byte[] cmd = new byte[2];
        cmd[0] = CMD_TALK_TO_PORT[0];
        cmd[1] = (byte)talkingToPortBits;
        sendCommand(cmd);
    }
    
    //** turn on or turn off the burst mode of specified sensors */
    public void setBurst(int sensorBits){
        burstBits = (byte)sensorBits;
        byte[] cmd = new byte[2];
        
        //here, the burst speed is not took into account
        cmd[0] = CMD_SET_BURST_MODE[0];
        
        cmd[1] = (byte)burstBits;
        sendCommand(cmd);
    }
    
    /** method to handle SerialPortEventListener*/    
    public void serialEvent(SerialPortEvent event){
        if(event.getEventType()==SerialPortEvent.DATA_AVAILABLE) {
            processResponse();
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

