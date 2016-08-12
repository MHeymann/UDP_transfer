package filesender;

import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;
import java.io.Console;
import java.io.File;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import layouts.RelativeLayout;


public class Sender extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	/* For giving instructions on what to enter in the txt field below */

	private final String giveLoc = "Please give file location";
	private JLabel label = null;
	/* text field for entering file location */
	private JTextField tfLocation = null;

	private FocusListener flnLogin;
	private FocusListener fldLogin, fldConnect;
	/* For entering the ip and port number */
	private JTextField tfServerIP = null, tfPortNo = null;
	/* Buttons for actions to be performed */
	private JButton btnConnect = null, btnDisconnect = null, btnSend = null; 
	private JButton btnBrowse = null; 
	/* For displaying messages */
	private JTextArea taMessages = null, taUsers = null;
	/* current connection status */
	private boolean connected = false;
	/* The Client Listener */
	/*
	private ClientListener listener = null;
	*/
	/* The Client Speaker for sending messages to the server */
	/*
	private ClientSpeaker speaker = null;
	*/
	private String myName = null;

	/* The port to connect to */
	private int portNo = -1;
	/* The host ip address */
	private String hostAddress = null;

	/* Create a file chooser */
	private JFileChooser fc = null;
	
	public Sender(String host, int port) {
		super("Send File");
		this.portNo = port;
		this.hostAddress = host;
		this.fc = new JFileChooser();

		/* NorthPanel */
		JPanel northPanel = new JPanel(new GridLayout(3,1));
		/* Spacte to enter the server's name and port number */
		JPanel serverPortPanel = new JPanel(new GridLayout(1,5, 1,3));
		/* start up the text fields for server name and port number */
		tfServerIP = new JTextField(this.hostAddress);
		tfPortNo = new JTextField("" + this.portNo);
		tfPortNo.setHorizontalAlignment(SwingConstants.RIGHT);

		serverPortPanel.add(new JLabel("Server Address:  "));
		serverPortPanel.add(tfServerIP);
		serverPortPanel.add(new JLabel("Port Number:  "));
		serverPortPanel.add(tfPortNo);
		serverPortPanel.add(new JLabel(""));
		/* put all of this in the north pannel */
		northPanel.add(serverPortPanel);

		/* The label and text field for communication */
		label = new JLabel("Enter the location of the file you want to send:", SwingConstants.CENTER);
		northPanel.add(label);

		JPanel textFieldButtonPanel = new JPanel(new RelativeLayout(RelativeLayout.X_AXIS, 3));

		tfLocation = new JTextField(giveLoc);
		flnLogin = new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (tfLocation.getText().equals(giveLoc)) {
					tfLocation.setText("");
				}
			}
			public void focusLost(FocusEvent e) {
				if (tfLocation.getText().equals("")) {
					tfLocation.setText(giveLoc);
				}
			}
		};
		tfLocation.addFocusListener(flnLogin);
		tfLocation.setBackground(Color.WHITE);
		tfLocation.setEditable(false);

		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(this);
		btnBrowse.setEnabled(false);

		textFieldButtonPanel.add(tfLocation, new Float(5));
		textFieldButtonPanel.add(btnBrowse, new Float(1));

		/* put all of this in the north pannel */
		northPanel.add(textFieldButtonPanel);
		this.add(northPanel, BorderLayout.NORTH);

		/* 
		 * The CenterPanel where chat's are displayed and online users
		 * shown
		 */
		taMessages = new JTextArea("Message area:\n", 80, 80);
		taUsers = new JTextArea("\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(taMessages));
		centerPanel.add(new JScrollPane(taUsers));
		taMessages.setEditable(false);
		taUsers.setEditable(false);
		this.add(centerPanel, BorderLayout.CENTER);

		JPanel southPanel = new JPanel();

		/* the 3 buttons */
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(this);
		btnSend = new JButton("Send");
		btnSend.addActionListener(this);
		btnSend.setEnabled(false);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(this);
		btnDisconnect.setEnabled(false);

		southPanel.add(btnConnect);
		southPanel.add(btnSend);
		southPanel.add(btnDisconnect);

		this.add(southPanel, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(800, 600);
		this.setVisible(true);


		tfLocation.addActionListener(this);

		btnConnect.requestFocus();
	}

	public void append(String s) 
	{
		taMessages.append(s);
		taMessages.setCaretPosition(taMessages.getText().length() - 1);
	}

	public void showOnlineUsers(Set<String> users) {
		int i;
		String[] userArray = new String[users.size()];

		i = 0;
		for (String s: users){
			userArray[i] = s;
			i++;
		}

		Arrays.sort(userArray);

		taUsers.setText("");
		taUsers.append("Online Users:\n");
		for (String s: userArray) {
			taUsers.append(s + "\n");
		}
	}

	public void brokenConnection() {
		btnConnect.setEnabled(true);
		btnSend.setEnabled(false);
		btnDisconnect.setEnabled(false);

		tfLocation.setEditable(false);
		btnBrowse.setEnabled(false);
		label.setText("Enter your Username and password below");
		tfPortNo.setText("" + this.portNo);
		tfServerIP.setText(this.hostAddress);
		tfServerIP.setEditable(true);
		tfPortNo.setEditable(true);
		/*
		tfLocation.removeActionListener(this);
		*/
		connected = false;
		taMessages.setText("");
		taUsers.setText("");
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		int returnval;
		File file = null;

		/* Disconnect being the button */
		if (o == btnDisconnect) {
			/*
			speaker.logoff();
			*/
			this.brokenConnection();
			return;

		} else if (o == btnSend) {
			if (connected) {
				/* btnSending message */
				String rname = tfLocation.getText();

				this.append(rname);
				/*
				if (speaker.sendString(mtext, rname)) {
					this.append(this.myName + " to " + rname + ": " + mtext + "\n");
				} else {
					this.append("Some error sending message\n");
				}
					
				tfLocation.setText(giveLoc);
				*/
				return;
			} else {
				/* broken connection, attempt to reconnect */
			}
		} else if (o == btnBrowse) {
			returnval = fc.showOpenDialog(this);
			file = fc.getSelectedFile();
			tfLocation.setText(file.getName());

		} else if (o == btnConnect) {
			String fileLocation = tfLocation.getText().trim();
			if (fileLocation.length() == 0) {
				return;
			}
			
			String server = tfServerIP.getText().trim();
			if (server.length() == 0) {
				return;
			}

			String portNumber = tfPortNo.getText().trim();
			if (portNumber.length() == 0) {
				return;
			}
			int port = -1;
			try {
				port = Integer.parseInt(portNumber);
			} catch (Exception except) {
				System.err.printf("Please provide a valid port number\n");
				return;
			}
			
			/*
			this.speaker = new ClientSpeaker(fileLocation, server, port, true);
			*/
			this.myName = fileLocation;
			/* open connection if possible */

			/*
			if (!this.speaker.btnConnect(password)) {
				return;
			}
			*/

			/*
			this.listener = new ClientListener(this.speaker.getSocketChannel(), this, fileLocation);
			Thread thread = new Thread(listener);
			thread.start();
			*/

			connected = true;

			btnConnect.setEnabled(false);
			btnSend.setEnabled(true);
			btnDisconnect.setEnabled(true);
			tfLocation.setEditable(true);
			btnBrowse.setEnabled(true);

			tfServerIP.setEditable(false);
			tfPortNo.setEditable(false);
			/*
			tfLocation.addActionListener(this);
			*/

			this.setTitle(this.getTitle() + " - " + this.myName);

		}
	}

	public static String getPassword() {
		String line = null;
		Console cons = null;
		char[] passwd = null;

		if ((cons = System.console()) != null &&
				(passwd = cons.readPassword()) != null) {
			line = new String(passwd);
			java.util.Arrays.fill(passwd, ' ');
		}
		return line;
	}

    public static void main(String[] args)  {
		Sender sender = null;
		sender = new Sender("localhost", 8002);
		System.out.println("made a sender");
	}
}
