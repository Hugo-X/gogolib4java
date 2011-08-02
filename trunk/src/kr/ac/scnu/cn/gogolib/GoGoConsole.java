package kr.ac.scnu.cn.gogolib;

import java.io.*;
import java.util.Vector;
/** 
 * a Command-Line application to manipulate local connected GoGo Board
 **/
public class GoGoConsole extends GoGoSerialPort
{

    public ConsoleThread innerConsoleThread;
    private int[] sensorValues;
    
    public GoGoConsole(){
    	sensorValues = new int[8];
    	for(int i=0; i<8; i++)
    		sensorValues[i] = -1;
    }
    
    @Override
	public void burstResponseHandle(byte[] responseBytes) {		
    	//System.out.println(getHexString(responseBytes));
    	int sensorID = (responseBytes[0] & 0xE0) >> 5;   //sss0-00xx xxxx-xxxx
    	int highByte = (responseBytes[0] & 0x03) << 8;
    	int lowByte  = responseBytes[1] & 0xFF;
    	int value = highByte + lowByte;
    	sensorValues[sensorID] = value;
	}

	@Override
	public void cmdResponseHandle(byte[] responseBytes) {
		switch(responseBytes.length){
			case 1:
				System.out.println(getHexString(responseBytes));
				break;
				
			case 2:// response for sensor reading
				System.out.println(((responseBytes[0]&0x03)<<8) + (responseBytes[1]&0xFF));
				break;
				
			case 4:
				System.out.println(getHexString(responseBytes));
				break;
		}
	}
	
	public void connect(){
		Vector<String> nameList=listSerialPortNames();
		System.out.println("--Serialporst Name List: ");
		for(String name:nameList){
			System.out.print(name+" ");
		}
		System.out.print("\n--please input the name of port you want to use: ");
		BufferedReader insReader = new BufferedReader(new InputStreamReader(System.in));
		String portName = null;
		
		try {
			portName = insReader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setPort(portName.toUpperCase());
		
        try{
        	connectBoard("GoGo Console", 2000, 6000);
        }catch(Exception ex){
            ex.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("--Ready for command--");
	}
	/** interpret console command and return the response type of this command */
    private int consoleCommandInterpreter(String inputCommand){
        String[] cmds;
        String cmd, arg;
                
        cmds = inputCommand.split("[\\s,]+");
        
        cmd = cmds[0];
        if(cmds.length > 1)
            arg = cmds[1];
        else
            arg = null;
            
        if(cmd.equalsIgnoreCase("quit")||cmd.equalsIgnoreCase("exit"))
            return 1;

        if(cmd.equalsIgnoreCase("display")){        	
	    	for(int i=0; i<7; i++){
	    		System.out.print(sensorValues[i]+" ");
	    	}
	    	System.out.print(sensorValues[7]+" ");
	    	return 0;
        }
        
        if(cmd.equalsIgnoreCase("beep")){
            beep();
            return 0;
        }
        
        if(cmd.equalsIgnoreCase("ledon")){
            ledOn();            
            return 0;
        }
        
        if(cmd.equalsIgnoreCase("ledoff")){
            ledOff();
            return 0;
        }
            
        if(cmd.equalsIgnoreCase("motoron")){
            motorOn();
            return 0;
        }
        
        if(cmd.equalsIgnoreCase("motoroff")){
            motorOff();
            return 0;
        }
        
        if(cmd.equalsIgnoreCase("ping")){
            ping();
            return 0;
        }
        
        if(cmd.equalsIgnoreCase("sensor")){
            if(arg == null)
                return -1;
            readSensor(Integer.parseInt(arg));
            return 0;
        }
        
        if(cmd.equalsIgnoreCase("burst")){
            if(arg == null)
                return -1;
            int burstBits = Integer.parseInt(arg);
            setBurst(burstBits);
            return 0;
        }
        
        // invalid command
        return -1;
    }    
    
    
    /** inner class of ConsoleThread */    
    private class ConsoleThread extends Thread{    
        String inputCommand = null;
        int reCode = 0;
        public void run(){            
        	BufferedReader insReader = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                //consoleOut.print(">>");
                try{
                    inputCommand = insReader.readLine();
                }catch(IOException ex){
                    ex.printStackTrace();
                }
                inputCommand.trim();
                if(inputCommand == null || inputCommand.equals(""))
                    continue;
                
                reCode = consoleCommandInterpreter(inputCommand);
                
                if(reCode == 1)
                	break;
                else if(reCode == -1)
                	System.out.println("invalid command");
            }
        }
    }
    
    public void startConsole(){
    	startSPCP();    	
    	// start the console, commandResponse, burstResponse threads
    	innerConsoleThread = new ConsoleThread();
    	innerConsoleThread.start();        
        
        // finish until the innerConsoleThread terminated
        try {
			innerConsoleThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    }
    
    public void stopConsole(){
		stopSPCP();
		disconnectBoard();
    }
    
    public static void main(String[] args){
    	
        GoGoConsole ggc = new GoGoConsole();
        
        //GoGoSerialPort.debugOut = System.err;
        
        ggc.connect();
        ggc.startConsole();
        ggc.stopConsole();
        System.exit(0); // exit the application        
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
	
}


