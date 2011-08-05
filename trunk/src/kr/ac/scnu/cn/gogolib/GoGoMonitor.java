package kr.ac.scnu.cn.gogolib;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Dimension;
import javax.swing.JPanel;

import java.awt.Rectangle;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import javax.swing.JTextPane;
import java.awt.GridLayout;
/** 
 * a GUI application to manipulate local connected GoGo Board
 **/

public class GoGoMonitor extends GoGoSerialPort
{
	JTextField[] jTextFieldArray;
	int[] sensorValueArray;
	JCheckBox[] jCheckBoxArray;
	private JButton jButtonBeep = null;
	private JFrame jFrame = null;  //  @jve:decl-index=0:visual-constraint="139,41"
	private JPanel jContentPane = null;
	private JButton jButtonPing = null;
	
	private JTextField jTextSensor0 = null;
	private JTextField jTextSensor1 = null;
	private JTextField jTextSensor2 = null;	
	private JTextField jTextSensor3 = null;
	private JTextField jTextSensor4 = null;
	private JTextField jTextSensor5 = null;
	private JTextField jTextSensor6 = null;
	private JTextField jTextSensor7 = null;	
	private JToggleButton jToggleLED = null;
	private JLabel jLabelAboutBoard = null;
	private JButton jButtonBurst = null;
	private JToggleButton jToggleButtonConnect = null;
	private JComboBox jComboBoxPortNames = null;
	private JCheckBox jCheckBoxSensor1 = null;
	private JCheckBox jCheckBoxSensor2 = null;
	private JCheckBox jCheckBoxSensor3 = null;
	private JCheckBox jCheckBoxSensor4 = null;
	private JCheckBox jCheckBoxSensor0 = null;
	private JCheckBox jCheckBoxSensor5 = null;
	private JCheckBox jCheckBoxSensor6 = null;
	private JCheckBox jCheckBoxSensor7 = null;
	private JDialog jDialog = null;  //  @jve:decl-index=0:visual-constraint="605,96"
	private JPanel jContentPane1 = null;
	private JTextPane jTextPane = null;

	int ticks = 0;
	
	public void updateSensorTxt(){
		
    	for(int i=0; i<8; i++){
    		if(sensorValueArray[i]!=-1)
    			jTextFieldArray[i].setText(""+sensorValueArray[i]);
    		else
    			jTextFieldArray[i].setText("N/A");
    	}
	}
	@Override
	public void burstResponseHandle(byte[] responseBytes){
    	int sensorID = (responseBytes[0] & 0xE0) >> 5;   //sss0-00xx xxxx-xxxx
    	int highByte = (responseBytes[0] & 0x03) << 8;
    	int lowByte  = responseBytes[1] & 0xFF;
    	int value = highByte + lowByte;
 	
		sensorValueArray[sensorID] = value;
    	
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
			{
				String boardTypeTxt = "Unknown Brd";
				String hwVerTxt, fwVerTxt;
				
				int hwVersionByte = responseBytes[2]&0xFF;
				hwVerTxt = "Hw: " + (hwVersionByte / 16) + "." + (hwVersionByte % 16);
				
				fwVerTxt = "Fw: " + (responseBytes[3] & 0xFF);

				switch(responseBytes[1]&0xFF){
					case 1:
						boardTypeTxt = "GoGo Board";
						break;
					case 2:
						boardTypeTxt ="KoKo Board";
						break;
				}
				
				jLabelAboutBoard.setText(boardTypeTxt+" "+hwVerTxt+" "+fwVerTxt);
				break;
			}
		}
	}	
	
	public GoGoMonitor(){
		
		if(sensorValueArray==null)
    		sensorValueArray = new int[8];
		for(int i=0; i<8; i++)
			sensorValueArray[i] = -1;
	}
	
	/**
	 * This method initializes jButtonBeep	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonBeep() {
		if (jButtonBeep == null) {
			jButtonBeep = new JButton();
			jButtonBeep.setText("Beep");
			jButtonBeep.setBounds(new Rectangle(8, 16, 105, 25));
			jButtonBeep.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
						beep();
				}
			});			
		}
		return jButtonBeep;
	}

	/**
	 * This method initializes jFrame	
	 * 	
	 * @return javax.swing.JFrame	
	 */
	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setLocationRelativeTo(null);
			jFrame.setSize(new Dimension(442, 238));
			jFrame.setTitle("GoGo Monitor by Java");
			jFrame.setContentPane(getJContentPane());
			jFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					exit();
				}
			});
		}
		
		//map the sensor TextFiled
		jTextFieldArray = new JTextField[8];	
		jTextFieldArray[0] = jTextSensor0;
		jTextFieldArray[1] = jTextSensor1;
		jTextFieldArray[2] = jTextSensor2;
		jTextFieldArray[3] = jTextSensor3;
		jTextFieldArray[4] = jTextSensor4;
		jTextFieldArray[5] = jTextSensor5;
		jTextFieldArray[6] = jTextSensor6;
		jTextFieldArray[7] = jTextSensor7;
		
		jCheckBoxArray = new JCheckBox[8];	
		jCheckBoxArray[0] = jCheckBoxSensor0;
		jCheckBoxArray[1] = jCheckBoxSensor1;
		jCheckBoxArray[2] = jCheckBoxSensor2;
		jCheckBoxArray[3] = jCheckBoxSensor3;
		jCheckBoxArray[4] = jCheckBoxSensor4;
		jCheckBoxArray[5] = jCheckBoxSensor5;
		jCheckBoxArray[6] = jCheckBoxSensor6;
		jCheckBoxArray[7] = jCheckBoxSensor7;
		return jFrame;
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabelAboutBoard = new JLabel();
			jLabelAboutBoard.setBounds(new Rectangle(144, 16, 273, 23));
			jLabelAboutBoard.setText("");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getjTextSensor0(), null);
			jContentPane.add(getJButtonBeep(), null);
			jContentPane.add(getJButtonPing(), null);			
			jContentPane.add(getJTextSensor1(), null);
			jContentPane.add(getJTextSensor2(), null);
			jContentPane.add(getJTextSensor3(), null);
			jContentPane.add(getJTextSensor4(), null);
			jContentPane.add(getJTextSensor5(), null);
			jContentPane.add(getJTextSensor6(), null);
			jContentPane.add(getJTextSensor7(), null);			
			jContentPane.add(getJToggleLED(), null);
			jContentPane.add(jLabelAboutBoard, null);
			jContentPane.add(getJButtonBurst(), null);
			jContentPane.add(getJToggleButtonConnect(), null);
			jContentPane.add(getJComboBoxPortNames(), null);
			jContentPane.add(getJCheckBoxSensor1(), null);
			jContentPane.add(getJCheckBoxSensor2(), null);
			jContentPane.add(getJCheckBoxSensor3(), null);
			jContentPane.add(getJCheckBoxSensor4(), null);
			jContentPane.add(getJCheckBoxSensor0(), null);
			jContentPane.add(getJCheckBoxSensor5(), null);
			jContentPane.add(getJCheckBoxSensor6(), null);
			jContentPane.add(getJCheckBoxSensor7(), null);
		}
		return jContentPane;
	}
	
	public void exit(){
		System.exit(0);
	}
	
	/**
	 * This method initializes jButtonPing	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonPing() {
		if (jButtonPing == null) {
			jButtonPing = new JButton();
			jButtonPing.setBounds(new Rectangle(8, 48, 105, 25));
			jButtonPing.setText("Ping");
			jButtonPing.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ping();
				}
			});
		}
		return jButtonPing;
	}

	/**
	 * This method initializes jTextSensor2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextSensor2() {
		if (jTextSensor2 == null) {
			jTextSensor2 = new JTextField();
			jTextSensor2.setBounds(new Rectangle(288, 56, 61, 37));
			jTextSensor2.setText("N/A");
			jTextSensor2.setEditable(false);
		}
		return jTextSensor2;
	}

	/**
	 * This method initializes jTextSensor1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextSensor1() {
		if (jTextSensor1 == null) {
			jTextSensor1 = new JTextField();
			jTextSensor1.setBounds(new Rectangle(216, 56, 61, 37));
			jTextSensor1.setText("N/A");
			jTextSensor1.setEditable(false);
		}
		return jTextSensor1;
	}

	/**
	 * This method initializes jTextSensor3	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextSensor3() {
		if (jTextSensor3 == null) {
			jTextSensor3 = new JTextField();
			jTextSensor3.setBounds(new Rectangle(360, 56, 61, 37));
			jTextSensor3.setText("N/A");
			jTextSensor3.setEditable(false);
		}
		return jTextSensor3;
	}

	/**
	 * This method initializes jTextSensor4	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextSensor4() {
		if (jTextSensor4 == null) {
			jTextSensor4 = new JTextField();
			jTextSensor4.setBounds(new Rectangle(144, 104, 60, 37));
			jTextSensor4.setText("N/A");
			jTextSensor4.setEditable(false);
		}
		return jTextSensor4;
	}

	/**
	 * This method initializes jTextSensor5	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextSensor5() {
		if (jTextSensor5 == null) {
			jTextSensor5 = new JTextField();
			jTextSensor5.setBounds(new Rectangle(216, 104, 61, 37));
			jTextSensor5.setText("N/A");
			jTextSensor5.setEditable(false);
		}
		return jTextSensor5;
	}

	/**
	 * This method initializes jTextSensor6	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextSensor6() {
		if (jTextSensor6 == null) {
			jTextSensor6 = new JTextField();
			jTextSensor6.setBounds(new Rectangle(288, 104, 61, 37));
			jTextSensor6.setText("N/A");
			jTextSensor6.setEditable(false);
		}
		return jTextSensor6;
	}

	/**
	 * This method initializes jTextSensor7	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextSensor7() {
		if (jTextSensor7 == null) {
			jTextSensor7 = new JTextField();
			jTextSensor7.setBounds(new Rectangle(360, 104, 61, 37));
			jTextSensor7.setText("N/A");
			jTextSensor7.setEditable(false);
		}
		return jTextSensor7;
	}

	/**
	 * This method initializes jTextSensor0	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getjTextSensor0() {
		if (jTextSensor0 == null) {
			jTextSensor0 = new JTextField();
			jTextSensor0.setBounds(new Rectangle(144, 56, 61, 37));
			jTextSensor0.setText("N/A");
			jTextSensor0.setEditable(false);
		}
		return jTextSensor0;
	}

	/**
	 * This method initializes jToggleLED	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJToggleLED() {
		if (jToggleLED == null) {
			jToggleLED = new JToggleButton();
			jToggleLED.setBounds(new Rectangle(8, 80, 105, 25));
			jToggleLED.setText("LED ON/OFF");
			jToggleLED.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if(jToggleLED.isSelected())
						ledOn();
					else
						ledOff();
				}
			});
		}
		return jToggleLED;
	}

	/**
	 * This method initializes jButtonBurst	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonBurst() {		
		if (jButtonBurst == null) {
			jButtonBurst = new JButton();
			jButtonBurst.setBounds(new Rectangle(296, 152, 105, 41));
			jButtonBurst.setText("Set Burst ");
			jButtonBurst.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setBurst(getBurstBits());
				}
			});
		}
		return jButtonBurst;
	}

	private int getBurstBits(){
		int burstBits = 0;
		
		for (int i=0; i<8; i++){
			if(jCheckBoxArray[i].isSelected())
				burstBits |= 1<<i;
			else
				sensorValueArray[i] = -1;
		}		
		
		return burstBits;
	}
	public void startMonitor(){
		//System.out.println("selected port name: " + jComboBoxPortNames.getSelectedItem());
		setPort(jComboBoxPortNames.getSelectedItem().toString());
		try {
			connectBoard("GoGo Monitor", 2000, 6000);
			startSPCP();
			setEnableButtons(true);
		} catch (Exception e) {
			this.jToggleButtonConnect.setSelected(false);
			this.jTextPane.setText(e.getMessage());
			this.jDialog.setModal(true);
			//this.jDialog.set
			this.jDialog.setVisible(true);
		}
	}
	
	public void setEnableButtons(boolean b){
		this.jButtonBeep.setEnabled(b);
		this.jButtonBurst.setEnabled(b);
		this.jButtonPing.setEnabled(b);
		this.jToggleLED.setEnabled(b);
	}
	
	public void stopMonitor(){
		//disconnect
		stopSPCP();
		disconnectBoard();
		
		//clear UI
		jLabelAboutBoard.setText("");
		setEnableButtons(false);
		for(int i=0; i<8; i++){
			sensorValueArray[i] = -1;
			this.jCheckBoxArray[i].setSelected(false);
		}
		
		
	}
	
	/**
	 * This method initializes jToggleButtonConnect	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getJToggleButtonConnect() {
		if (jToggleButtonConnect == null) {
			jToggleButtonConnect = new JToggleButton();
			jToggleButtonConnect.setBounds(new Rectangle(8, 152, 153, 41));
			jToggleButtonConnect.setText("Connect/Disconnect");			
			jToggleButtonConnect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(jToggleButtonConnect.isSelected()){
						startMonitor();
					}else{
						stopMonitor();
					}
				}
			});
		}
		return jToggleButtonConnect;
	}

	/**
	 * This method initializes jComboBoxPortNames	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getJComboBoxPortNames() {
		if (jComboBoxPortNames == null) {
			jComboBoxPortNames = new JComboBox(GoGoSerialPort.listSerialPortNames());
			jComboBoxPortNames.setBounds(new Rectangle(8, 120, 81, 25));
		}
		return jComboBoxPortNames;
	}

	/**
	 * This method initializes jCheckBoxSensor1	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSensor1() {
		if (jCheckBoxSensor1 == null) {
			jCheckBoxSensor1 = new JCheckBox();
			jCheckBoxSensor1.setBounds(new Rectangle(200, 152, 21, 21));
		}
		return jCheckBoxSensor1;
	}

	/**
	 * This method initializes jCheckBoxSensor2	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSensor2() {
		if (jCheckBoxSensor2 == null) {
			jCheckBoxSensor2 = new JCheckBox();
			jCheckBoxSensor2.setBounds(new Rectangle(224, 152, 21, 21));
		}
		return jCheckBoxSensor2;
	}

	/**
	 * This method initializes jCheckBoxSensor3	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSensor3() {
		if (jCheckBoxSensor3 == null) {
			jCheckBoxSensor3 = new JCheckBox();
			jCheckBoxSensor3.setBounds(new Rectangle(248, 152, 21, 21));
		}
		return jCheckBoxSensor3;
	}

	/**
	 * This method initializes jCheckBoxSensor4	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSensor4() {
		if (jCheckBoxSensor4 == null) {
			jCheckBoxSensor4 = new JCheckBox();
			jCheckBoxSensor4.setBounds(new Rectangle(176, 176, 21, 21));
		}
		return jCheckBoxSensor4;
	}

	/**
	 * This method initializes jCheckBoxSensor0	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSensor0() {
		if (jCheckBoxSensor0 == null) {
			jCheckBoxSensor0 = new JCheckBox();
			jCheckBoxSensor0.setBounds(new Rectangle(176, 152, 21, 21));
		}
		return jCheckBoxSensor0;
	}

	/**
	 * This method initializes jCheckBoxSensor5	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSensor5() {
		if (jCheckBoxSensor5 == null) {
			jCheckBoxSensor5 = new JCheckBox();
			jCheckBoxSensor5.setBounds(new Rectangle(200, 176, 21, 21));
		}
		return jCheckBoxSensor5;
	}

	/**
	 * This method initializes jCheckBoxSensor6	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSensor6() {
		if (jCheckBoxSensor6 == null) {
			jCheckBoxSensor6 = new JCheckBox();
			jCheckBoxSensor6.setBounds(new Rectangle(224, 176, 21, 21));
		}
		return jCheckBoxSensor6;
	}

	/**
	 * This method initializes jCheckBoxSensor7	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSensor7() {
		if (jCheckBoxSensor7 == null) {
			jCheckBoxSensor7 = new JCheckBox();
			jCheckBoxSensor7.setBounds(new Rectangle(248, 176, 21, 21));
		}
		return jCheckBoxSensor7;
	}

	/**
	 * This method initializes jDialog	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	private JDialog getJDialog() {
		if (jDialog == null) {			
			jDialog = new JDialog(getJFrame());
			jDialog.setSize(new Dimension(241, 119));
			jDialog.setTitle("Exception");
			jDialog.setContentPane(getJContentPane1());
			jDialog.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					System.out.println("windowClosing()"); // TODO Auto-generated Event stub windowClosing()
					jDialog.setVisible(false);
				}
			});
			jDialog.setLocationRelativeTo(getJFrame());
		}
		return jDialog;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane1() {
		if (jContentPane1 == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			jContentPane1 = new JPanel();
			jContentPane1.setLayout(gridLayout);
			jContentPane1.add(getJTextPane(), null);
		}
		return jContentPane1;
	}

	/**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane();
			jTextPane.setEditable(false);
		}
		return jTextPane;
	}

	public static void main(String[] args) 
	{
		GoGoMonitor ggm = new GoGoMonitor();        
		ggm.getJFrame().setVisible(true);
		ggm.getJDialog();
		ggm.setEnableButtons(false);
		
		while(true){
			ggm.updateSensorTxt();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
        //GoGoSerialPort.debugOut=System.err;
	}
	
}
