package kr.ac.scnu.cn.gogolib;

import java.io.*;

public class GoGoConsole
{
    public GoGoSerialPort ggsp;
    public ConsoleThread innerConsoleThread;
    public GetCommandResponseThread innerCommandResponseThread;
    public GetBurstResponseThread innerBurstResponseThread;
    
    public BufferedReader consoleIn;
    public PrintStream consoleOut;
    //public OutputStream burstOut;
    public boolean[] cmdIsReplied;
    public boolean[] burstOn;
        
    /** initialize the console */
    public GoGoConsole(InputStream input, PrintStream output){
        ggsp = new GoGoSerialPort();
        
        consoleIn = new BufferedReader(new InputStreamReader(input));
        consoleOut = output;
        //burstOut = new OutputStream();
        
        innerConsoleThread = new ConsoleThread();
        innerCommandResponseThread = new GetCommandResponseThread();
        innerBurstResponseThread = new GetBurstResponseThread();
        cmdIsReplied = new boolean[1];
        cmdIsReplied[0] = true;
        burstOn = new boolean[1];
        burstOn[0] = false;
    }
    
    /** interprete console command and return the response type of this command */
    public int consoleCommandInterpreter(String inputCommand){
        String[] cmds;
        String cmd, arg;
        
        inputCommand.trim();
        cmds = inputCommand.split("[\\s,]+");
        
        cmd = cmds[0];
        if(cmds.length > 1)
            arg = cmds[1];
        else
            arg = null;
            
        if(cmd.equalsIgnoreCase("quit")||cmd.equalsIgnoreCase("exit"))
            return 0;
            
        if(cmd.equalsIgnoreCase("beep")){
            ggsp.beep();
            return 1;
        }
        
        if(cmd.equalsIgnoreCase("ledon")){
            ggsp.ledOn();
            return 1;
        }
        
        if(cmd.equalsIgnoreCase("ledoff")){
            ggsp.ledOff();
            return 1;
        }
            
        if(cmd.equalsIgnoreCase("motoron")){
            ggsp.motorOn();
            return 1;
        }
        
        if(cmd.equalsIgnoreCase("motoroff")){
            ggsp.motorOff();
            return 1;
        }
        
        if(cmd.equalsIgnoreCase("ping")){
            ggsp.ping();
            return 4;
        }
        
        if(cmd.equalsIgnoreCase("sensor")){
            if(arg == null)
                return -1;
            ggsp.readSensor(Integer.parseInt(arg));
            return 2;
        }
        
        if(cmd.equalsIgnoreCase("burst")){
            if(arg == null)
                return -1;
            int burstBits = Integer.parseInt(arg);
            byte[] b = {(byte)burstBits};
            consoleOut.println("burstBits = " + getHexString(b));
            ggsp.setBurst(burstBits);
            synchronized(burstOn){
                if(burstBits > 0){
                    burstOn[0] = true;
                    burstOn.notify();
                }else{
                    burstOn[0] = false;            
                }
            }
            return 1;
        }
        
        // invalid command
        return -1;
    }
    
    /** connect to the gogoboard specified by portName*/
    public void connectBoard(String appName, String portName, int acquireTimeOut, int receiveTimeOut){
        ggsp.setPort(portName);
        try{
            ggsp.connectBoard(portName, acquireTimeOut, receiveTimeOut);
        }catch(Exception ex){
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    /** inner class of ConsoleThread */    
    public class ConsoleThread extends Thread{
    
        String inputCommand = null;
        
        public void exit(){
            //consoleOut.println("ConsoleThread is going to exit");
            if(innerCommandResponseThread.isAlive()){
                innerCommandResponseThread.interrupt();
                //ggc.consoleOut.println("interrupt innerCommandResponseThread");
            }
            
            if(innerBurstResponseThread.isAlive()){
                innerBurstResponseThread.interrupt();
                //ggc.consoleOut.println("interrupt innerBurstResponseThread");
            }            
            System.exit(0); // exit the application
        }
        
        public void run(){
            
            consoleOut.println("type quit to leave");            

            while(true){
                //consoleOut.print(">>");
                try{
                    inputCommand = consoleIn.readLine();
                }catch(IOException ex){
                    ex.printStackTrace();
                }
                
                if(inputCommand == null || inputCommand.equals(""))
                    continue;
                
                switch(consoleCommandInterpreter(inputCommand)){
                    case 0:
                        exit(); // to exit
                        break;
                    case 1:
                        ggsp.GoGoWaittingForDataSize = GoGoSerialPort.RES_BYTES_1;
                        break;
                    case 2:
                        ggsp.GoGoWaittingForDataSize = GoGoSerialPort.RES_BYTES_2;
                        break;
                    case 3:
                        break; // control burst mode
                    case 4:
                        ggsp.GoGoWaittingForDataSize = GoGoSerialPort.RES_BYTES_4;
                        break;
                    default:
                        consoleOut.println("InvalidCommand");
                        continue;
                }                
                ggsp.GoGoSPCPStatus = GoGoSerialPort.WAITTING_FOR_RES_OR_BURST_HEADER;                
                
                //innerBurstResponseThread.start();
                if(!innerCommandResponseThread.isAlive()){
                    //consoleOut.println("!innerCommandResponseThread is not Alive");
                    innerCommandResponseThread.start();
                }else{
                    //consoleOut.println("innerCommandResponseThread is Alive ");
                }
                
                synchronized(cmdIsReplied){
                    cmdIsReplied[0] = false;
                    cmdIsReplied.notify();
                    
                    try{
                        if(!cmdIsReplied[0])
                            cmdIsReplied.wait();
                    }catch(InterruptedException ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    /** inner class of GetCommandResponseThread */
    public class GetCommandResponseThread extends Thread{
        String responseHex;
        
        public void run(){            
            while(true){
                try{
                    //consoleOut.println("GetCommandResponseThread is running");
                    
                    synchronized(cmdIsReplied){
                        // do not need to get command responsed until cmdIsReplied is false
                        if(cmdIsReplied[0]){
                            //consoleOut.println("GetCommandResponseThread is waiting...");
                            cmdIsReplied.wait();
                        }
                        
                        responseHex = getHexString(ggsp.getCommandResponse());
                        consoleOut.println(responseHex);
                    
                        cmdIsReplied[0] = true;
                        cmdIsReplied.notify();
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
        public void run(){             
            while(true){
                try{
                    synchronized(burstOn){
                        if(!burstOn[0]){
                            consoleOut.println("GetBurstResponseThread is waiting...");
                            burstOn.wait();
                        }                       
                        burstResponseHandle(ggsp.getBurstResponse());                        
                    }                    
                }catch(InterruptedException ex){
                    //ex.printStackTrace();                    
                    break;
                }
            }
        }
    }
    
    /** display or print the burst response*/
    public void burstResponseHandle(byte[] responseBytes){
        String responseHex;        
        responseHex = getHexString(responseBytes);
        consoleOut.println("burst,"+responseHex);
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
    
    public static void main(String[] args){
                
        GoGoConsole ggc = new GoGoConsole(System.in, System.out);        
        ggc.connectBoard("GoGo Console", args[0], 2000, 6000);
        ggc.innerConsoleThread.start();
        ggc.innerBurstResponseThread.start();
        /*
        try{
            ggc.innerConsoleThread.join();        
        }catch(InterruptedException ex){
            ex.printStackTrace();
        }*/       
        
    }
}


