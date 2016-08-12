package filesender;

import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;
/*
import client.*;
import packet.*;
*/
import java.io.Console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * Author Murray Heymann
 *
 * This file is starts the client.  If it is started in gui mode, the swing
 * components are created and managed from here.  Threads for incoming and
 * outgoing data are created from here.  
 *
 * "LAFAYETTE!
 * I'm taking this horse
 * by the reins making
 * Redcoats redder with bloodstains"
 * Guns And Ships, Hamilton
 */
public class Sender extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	/* For giving instructions on what to enter in the txt field below */
	private JLabel label = null;
	/* text field for entering username and messages */
	private JTextField tfName = null;
	private JTextField tfData = null;

	private FocusListener flnLogin;
	private FocusListener fldLogin, fldConnect;
	/* For entering the ip and port number */
	private JTextField tfServerIP = null, tfPortNo = null;
	/* Buttons for actions to be performed */
	private JButton login = null, logout = null; 
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
	
	public Sender(String host, int port) {
		super("Send File");
		this.portNo = port;
		this.hostAddress = host;

		/* NorthPanel */
		JPanel northPanel = new JPanel(new GridLayout(4,1));
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
		label = new JLabel("Enter your Username:", SwingConstants.CENTER);
		northPanel.add(label);
		tfName = new JTextField("Name");
		tfData = new JTextField("Password");

		flnLogin = new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (tfName.getText().equals("Name")) {
					tfName.setText("");
				}
			}
			public void focusLost(FocusEvent e) {
				if (tfName.getText().equals("")) {
					tfName.setText("Name");
				}
			}
		};
		fldLogin = new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (tfData.getText().equals("Password")) {
					tfData.setText("");
				}
			}
			public void focusLost(FocusEvent e) {
				if (tfData.getText().equals("")) {
					tfData.setText("Password");
				}
			}
		};

		fldConnect = new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (tfData.getText().equals("Message")) {
					tfData.setText("");
				}
			}
			public void focusLost(FocusEvent e) {
				if (tfData.getText().equals("")) {
					tfData.setText("Message");
				}
			}
		};

		tfName.addFocusListener(flnLogin);
		tfData.addFocusListener(fldLogin);
		tfName.setBackground(Color.WHITE);
		tfData.setBackground(Color.WHITE);
		northPanel.add(tfName);
		northPanel.add(tfData);
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
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);

		southPanel.add(login);
		southPanel.add(logout);

		this.add(southPanel, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(800, 600);
		this.setVisible(true);


		tfName.addActionListener(this);
		tfData.addActionListener(this);

		tfName.requestFocus();
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
		login.setEnabled(true);
		logout.setEnabled(false);
		label.setText("Enter your Username and password below");
		tfName.setText("Name");
		tfData.setText("Password");
		tfData.removeFocusListener(fldConnect);
		tfData.addFocusListener(fldLogin);
		tfPortNo.setText("" + this.portNo);
		tfServerIP.setText(this.hostAddress);
		tfServerIP.setEditable(true);
		tfPortNo.setEditable(true);
		/*
		tfName.removeActionListener(this);
		tfData.removeActionListener(this);
		*/
		connected = false;
		taMessages.setText("");
		taUsers.setText("");
	}

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		/* logout being the button */
		if (o == logout) {
			/*
			speaker.logoff();
			*/
			this.brokenConnection();
			return;
		} else {
			/* some other buttons might be added here? */
		}

		if (o == tfName) {
			tfData.requestFocus();
			return;
		}
		if (connected) {
			/* sending message */
			String mtext = tfData.getText();
			String rname = tfName.getText();
			/*
			if (speaker.sendString(mtext, rname)) {
				this.append(this.myName + " to " + rname + ": " + mtext + "\n");
			} else {
				this.append("Some error sending message\n");
			}
			*/
				
			tfName.setText("Name");
			tfData.setText("Message");
			return;
		}

		if ((o == login) || (o == tfData)) {
			String username = tfName.getText().trim();
			if (username.length() == 0) {
				return;
			}
			
			String password = tfData.getText().trim();
			if (password.length() == 0) {
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
			this.speaker = new ClientSpeaker(username, server, port, true);
			*/
			this.myName = username;
			/* open connection if possible */

			/*
			if (!this.speaker.login(password)) {
				return;
			}
			*/

			/*
			this.listener = new ClientListener(this.speaker.getSocketChannel(), this, username);
			Thread thread = new Thread(listener);
			thread.start();
			*/

			tfData.setText("Message");
			tfData.removeFocusListener(fldLogin);
			tfData.addFocusListener(fldConnect);
			tfName.setText("Name");
			label.setText("Enter recipient and message, followed by <enter>, or choose an alternative action from the buttons below.");
			connected = true;

			login.setEnabled(false);
			logout.setEnabled(true);

			tfServerIP.setEditable(false);
			tfPortNo.setEditable(false);
			/*
			tfName.addActionListener(this);
			tfData.addActionListener(this);
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
		String line = null;
		String name = null;
		/*
		Thread threadSpeaker = null;
		Thread threadListen = null;
		ClientSpeaker speaker = null;
		ClientListener listener = null;
		*/
		Scanner scanner = new Scanner(System.in);
		
		if ((args.length >= 1) && args[0].equals("terminal")) {

			System.out.printf("Please enter your username: ");
			name = scanner.nextLine();
			/*
			speaker = new ClientSpeaker(name, "127.0.0.1", 8002, false);
			*/
	
			System.out.printf("Please provide the password for %s: ", name);
			line = getPassword();

			System.out.printf("%s with password %s \n", name, line);
			/*
			if (!speaker.login(line)) {
				return;
			}
			*/
			/*
			listener = new ClientListener(speaker.getSocketChannel());
			*/
	
			/*
			threadSpeaker = new Thread(speaker);
			threadListen = new Thread(listener);
			threadSpeaker.start();
			threadListen.start();
			*/
		} else {
			sender = new Sender("localhost", 8002);
			System.out.println("made a sender");
		}
	}
}
