package groept_kuleuven;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Enumeration;
import java.util.HashMap;

import gnu.io.*;

public class SerialCommunicator {

	Gui ui = null;
	private Enumeration<CommPortIdentifier> ports = null;
	private HashMap<String, CommPortIdentifier> portMap = new HashMap<String, CommPortIdentifier>();
	private CommPortIdentifier selectedPortIdentifier = null;
	private SerialPort serialPort = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private static boolean AD = false;
	private Range range;
	private Mode mode;

	public SerialCommunicator(Gui ui) {
		this.ui = ui;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	// Searches for ports and displays them in combobox
	public void searchForPorts() {
		ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements()) {
			CommPortIdentifier currentPort = (CommPortIdentifier) ports.nextElement();

			if (currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				ui.getSelectedPort().addItem(currentPort.getName());
				portMap.put(currentPort.getName(), currentPort);
			}
		}
	}

	// Opens the selected serial port and converts it to a serialPort object
	// Creates an inputStream and OutputStream object for the serialPort
	// Adds an eventlistener to the serialPort to check when there is data
	// availiable
	public void connect() {
		String selectedPort = (String) ui.getSelectedPort().getSelectedItem();
		selectedPortIdentifier = portMap.get(selectedPort);

		CommPort commPort = null;

		try {
			commPort = selectedPortIdentifier.open("Multimeter", 1000);
			serialPort = (SerialPort) commPort;

			ui.getOutputArea().setForeground(Color.black);
			ui.getOutputArea().append(selectedPort + " opened successfully" + "\n");

			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();

			serialPort.addEventListener(new SerialReader(inputStream, ui));
			serialPort.notifyOnDataAvailable(true);

		} catch (PortInUseException e) {
			ui.getOutputArea().setForeground(Color.red);
			ui.getOutputArea().append("Port is in use (" + e.toString() + ")" + "\n");
		} catch (Exception e) {
			ui.getOutputArea().setForeground(Color.red);
			ui.getOutputArea().append("Failed to open " + selectedPort + " (" + e.toString() + ")" + "\n");
		}
	}

	public void disconnect() {
		try {
			serialPort.removeEventListener();
			serialPort.close();
			inputStream.close();
			outputStream.close();

			ui.getOutputArea().setForeground(Color.black);
			ui.getOutputArea().append("Disconnected " + "\n");

		} catch (Exception e) {
			ui.getOutputArea().append("Failed to close " + serialPort.getName() + "(" + e.toString() + ")" + "\n");
		}
	}

	// SerialReader class which reads and displays the data when availiable
	public static class SerialReader implements SerialPortEventListener {
		private Gui ui;
		private InputStream inputStr;
//		private byte[] buffer = new byte[1024];

		public SerialReader(InputStream i, Gui ui) {
			this.ui = ui;
			this.inputStr = i;
		}

		@Override
		public void serialEvent(SerialPortEvent event) {
			int data;

			try {
				int i = 0;
				if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
					byte[] buffer = new byte[1024];
					while ((data = inputStr.read()) > -1 && i < buffer.length) {
						System.out.println(data);
						buffer[i] = (byte) data;
						byteToTextArea(buffer[i]);
						i++;
					}			
				//	bufferToTextArea(buffer);
				}
				
			} catch (IOException e) {
				ui.getOutputArea().setForeground(Color.red);
				ui.getOutputArea().append("There was an error while reading data.. (" + e.toString() + ")" + "\n");
			}
		}
	

	public void byteToTextArea(byte b) {
		if (AD) {
			//System.out.println(b);
			Byte c = (Byte) b;
			//System.out.println(c);
			int value = Byte.toUnsignedInt(c);
			//System.out.println(value + "\n");
			Range range = (Range) ui.getSelectedRange().getSelectedItem();
			double voltage = 0;
			switch (range) {
				case FIVE:
					
					voltage = (((double) value) / 255) * 5;
					if(voltage >= 2.509){
						voltage = ((voltage-2.509)/2)*5;
					} else{
						voltage = - (1-((voltage-1.431)/1.078))*5;
					}
					break;
					
				case THIRTY:
					voltage = (((double) value) / 255) * 25;
					if(voltage >= 12.549){
						voltage = ((voltage-12.549)/9.706)*25;
					}
					else{
						voltage = - (1-((voltage-0.588)/11.961))*25;
					}
					break;
				case SEVENFIFTY:
					voltage = ((double) value / 255) * 750;
					if(voltage>=350){
						voltage = ((voltage-350)/158.8)*750;
					}
					else{
						voltage = - (1-((voltage-82.4)/267.6))*750;
					}
					break;
					
				case EIGHT:
					
					voltage = ((double) value / 255) * 8;
					if(voltage>=4.016){
						voltage = ((voltage-4.016)/3.2)*8;
					}
					else{
						voltage = - (1-((voltage-0.314)/3.7))*8;
					}
					break;
				
					
				default:
					voltage = ((double) value / 255) * 5;
					System.out.println("Voltage range not selected (default = *5)" + "\n");
					break;
			}
			
			double voltageRounded = round(voltage, 3);
			ui.getOutputArea().setForeground(Color.black);
			switch(range){
				case FIVE:
				case EIGHT:
				case THIRTY:
					ui.getOutputArea().append(voltageRounded + "V" + "\n");
					break;
				case SEVENFIFTY:
					voltageRounded = round(voltage, 1);
					ui.getOutputArea().append(voltageRounded + "mV" + "\n");
					break;
				default: 
					ui.getOutputArea().append(voltageRounded + "V" + "\n");
					System.out.println("Voltage range not selected (default: append V)" + "\n");
					break;		
			}
			
			ui.getOutputArea().setCaretPosition(ui.getOutputArea().getDocument().getLength());
			
		} else {
			char output = (char) b;
			ui.getOutputArea().setForeground(Color.black);
			ui.getOutputArea().append(output + "\n");
			ui.getOutputArea().setCaretPosition(ui.getOutputArea().getDocument().getLength());

		}
	}

	public void bufferToTextArea(byte[] buffer) {
		if (AD) {
			for (int i = 0; i <= buffer.length; i++) {
				Byte b = (Byte) buffer[i];
				int value = Byte.toUnsignedInt(b);
				double voltage = ((double) value / 255) * 5;
				double voltageRounded = round(voltage, 3);
				ui.getOutputArea().setForeground(Color.black);
				ui.getOutputArea().append(voltageRounded + "V" + "\n");
				ui.getOutputArea().setCaretPosition(ui.getOutputArea().getDocument().getLength());
			}
			AD = false;
		} else {
			String output = new String(buffer, 0, buffer.length);
			ui.getOutputArea().setForeground(Color.black);
			ui.getOutputArea().append(output + "\n");
			ui.getOutputArea().setCaretPosition(ui.getOutputArea().getDocument().getLength());
		}
	}

	public double round(double value, int places) {
		if (places < 0){
			throw new IllegalArgumentException();
		}
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	}

	// writes data when enter is pressed or the send button is clicked
	// ToDo: include selected range
	public void write(String data) {

		try {

			if (data.equals("A")) {
				AD = true;
				byte[] buffer = data.getBytes();
				outputStream.write(buffer, 0, buffer.length);
			}

			else if (data.equals("H")) {
				byte[] buffer = new byte[2];
				buffer[1] = generateHeader();
				buffer[0] = 0x48;
				outputStream.write(buffer, 0, buffer.length);
			} else {
				byte[] buffer = data.getBytes();
				outputStream.write(buffer, 0, buffer.length);

			}

		} catch (IOException e) {
			ui.getOutputArea().setForeground(Color.red);
			ui.getOutputArea().append("There was an error while writing data.. (" + e.toString() + ")" + "\n");
		}
	}

	public byte generateHeader() {
		mode = (Mode) ui.getSelectedMode().getSelectedItem();
		range = (Range) ui.getSelectedRange().getSelectedItem();

		ui.getOutputArea().append("Selected mode: " + range.toString() + "  " + mode.toString() + "\n");
		byte b = 0x00;
		switch (mode) {
		case SINGLE:
			switch (range) {
			case FIVE:
				b = 0x01;
				break;
			case THIRTY:
				b = 0x11;
				break;
			case SEVENFIFTY:
				b = 0x21;
				break;
			default:
				b = 0x01;
				break;
			}
			break;

		case CONTINU:
			switch (range) {
			case FIVE:
				b = 0x02;
				break;
			case THIRTY:
				b = 0x12;
				break;
			case SEVENFIFTY:
				b = 0x22;
				break;
			default:
				b = 0x02;
				break;
			}
			break;

		default:
			b = 0x01;
			break;
		}
		return b;
	}
}

//public class SerialReader implements Runnable{
//
//private InputStream inputStr;
//private Gui ui;
//private byte[] buffer = new byte[1024];
//
//public SerialReader(InputStream in,Gui ui){
//	this.inputStr = in;
//	this.ui = ui;
//}
//
//@Override
//public void run() {
//	int data;	
//	try{
//		Thread.sleep(500);
//		int i = 0;
//		while((data = inputStr.read())>-1 && i < buffer.length){
//			buffer[i] = (byte) data;
//			i++;
//		}
//		bufferToTextArea(buffer);
//	}
//	catch(IOException e){
//		ui.getOutputArea().setForeground(Color.red);
//		ui.getOutputArea().append("There was an error while reading data.. (" + e.toString() + ")" + "\n");
//	}
//	catch(InterruptedException e){
//		System.out.println(e.toString());
//	}
//				
//}
//
//}
