import kr.ac.scnu.cn.gogolib.*;

import java.awt.Container;
import java.awt.GridLayout;
import java.io.*;

import javax.swing.JFrame;
import javax.swing.JTextField;


public class GoGoMonitor extends GoGoConsole
{
	JFrame monitorFrame;
	JTextField[] TextSensorValue;
	
    public void burstResponseHandle(byte[] responseBytes){
//    	System.out.println();
    	int sensorID = (responseBytes[0] & 0x70) >> 5;   //sss0-00xx xxxx-xxxx
    	int value = (responseBytes[0]& 0x03) << 8 + (responseBytes[1]);
    	TextSensorValue[sensorID].setText(sensorID+":"+value);
    }
    
	public GoGoMonitor(InputStream input, PrintStream output){
		super(input, output);
		
		monitorFrame = new JFrame("GoGo Monitor");
		monitorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = monitorFrame.getContentPane();
		
		content.setLayout(new GridLayout(1,8));
		TextSensorValue = new JTextField[8];
		
		for(int i=0; i<8; i++){
			TextSensorValue[i] = new JTextField(""+i);
			content.add(TextSensorValue[i]);
		}
		monitorFrame.setSize(800, 200);
		monitorFrame.setVisible(true);
	}
	
	public static void main(String[] args) 
	{
		GoGoMonitor ggm = new GoGoMonitor(System.in, System.out);        
        ggm.connectBoard("GoGo Monitor", args[0], 2000, 6000);
        ggm.innerConsoleThread.start();
        ggm.innerBurstResponseThread.start();
	}	
}
