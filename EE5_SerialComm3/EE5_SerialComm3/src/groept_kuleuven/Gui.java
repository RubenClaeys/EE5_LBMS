package groept_kuleuven;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;


public class Gui {

	private SerialCommunicator communicator;
	private JFrame frame;
	private JComboBox<String> portSelect;
	private JComboBox<Range> rangeSelect;
	private JComboBox<Mode> modeSelect;
	private JButton btnDisconnect;
	private JTextArea output;
	private JTextField input;
	private Oscilloscope chart;

	
	

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Gui() {
		initialize();
		communicator = new SerialCommunicator(this);
		communicator.searchForPorts();
	}
	

	public JComboBox<String> getSelectedPort() {
		return portSelect;
	}
	
	
	public JComboBox<Range> getSelectedRange() {
		return rangeSelect;
	}
	
	public JComboBox<Mode> getSelectedMode() {
		return modeSelect;
	}

	public JTextArea getOutputArea() {
		return output;
	}


	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 551, 434);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblSelect = new JLabel("Select a serial port:");
		lblSelect.setBounds(10, 10, 200, 20);
		frame.getContentPane().add(lblSelect);

		portSelect = new JComboBox<String>();
		portSelect.setBounds(20, 40, 100, 20);
		frame.getContentPane().add(portSelect);

		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				communicator.connect();
			}
		});
		btnConnect.setBounds(140, 40, 100, 20);
		frame.getContentPane().add(btnConnect);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				communicator.disconnect();

			}
		});
		btnDisconnect.setBounds(250, 40, 100, 20);
		frame.getContentPane().add(btnDisconnect);
		
		JLabel lblSelectMode = new JLabel("Select operation mode:");
		lblSelectMode.setBounds(10, 97, 200,14 );
		frame.getContentPane().add(lblSelectMode);
		
		modeSelect = new JComboBox<Mode>(Mode.values());
		modeSelect.setBounds(20, 124, 100, 20);
		frame.getContentPane().add(modeSelect);
		
		rangeSelect = new JComboBox<Range>(Range.values());
		rangeSelect.setBounds(140, 124, 100, 20);
		frame.getContentPane().add(rangeSelect);
		
//		sampleSelect = new JComboBox<SampleFrequency>(SampleFrequency.values());
//		sampleSelect.setBounds(260, 124, 100, 20);
//		frame.getContentPane().add(sampleSelect);
		
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				communicator.write("H");			
			}
		});
		btnSave.setBounds(260, 124, 100, 20);
		frame.getContentPane().add(btnSave);

		
		JLabel lblInput = new JLabel("Input:");
		lblInput.setBounds(10, 160, 100, 20);
		frame.getContentPane().add(lblInput);
		
		input = new JTextField();
		input.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				communicator.write(input.getText());
				
			}
			
		});
		input.setBounds(20, 180, 100, 20);
		input.setColumns(10);
		frame.getContentPane().add(input);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				communicator.write(input.getText());			
			}
		});
		btnSend.setBounds(130, 180, 100, 20);
		frame.getContentPane().add(btnSend);


		
		JLabel lblOutput = new JLabel("Output: ");
		lblOutput.setBounds(10, 220, 69, 14);
		frame.getContentPane().add(lblOutput);
		
		output = new JTextArea();
		output.setEditable(false);
		output.setLineWrap(true);
			JScrollPane scrollPane = new JScrollPane(output);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setVisible(true);
			scrollPane.setBounds(20, 250, 350, 104);
		frame.getContentPane().add(scrollPane);
		
		JButton btnChart = new JButton("Oscilloscope");
		btnChart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				try {
					chart = new Oscilloscope();
					chart.getFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		btnChart.setBounds(380, 250, 120, 20);
		frame.getContentPane().add(btnChart);
		
		

		
		
		
	}
}
