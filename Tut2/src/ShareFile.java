
import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;
import filesender.*;
import filereceiver.*;
/*
import client.*;
import packet.*;
*/
import java.io.Console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * Author Murray Heymann & Ludwe Mtakati
 *
 * This file is starts the client.  If it is started in gui mode, the swing
 * components are created and managed from here.  Threads for incoming and
 * outgoing data are created from here.  
 *
 */
public class ShareFile extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	/* For giving instructions on what to enter in the txt field below */
	private JLabel label = null;

	/* For entering the ip and port number */
	private JTextField tfReceiverIP = null, tfPortNo = null;
	/* Buttons for actions to be performed */
	private JButton sendfile = null, receivefile = null; 
	/* current connection status */

	/* The port to connect to */
	private int portNo = -1;

	/* The host ip address */
	private String recAddress = null;
	
	public ShareFile(String host, int port) {
		super("Share File");
		this.portNo = port;
		this.recAddress = host;

		/* NorthPanel */
		JPanel northPanel = new JPanel(new GridLayout(1,1));
		/* Spacte to enter the server's name and port number */
		JPanel receiverPortPanel = new JPanel(new GridLayout(1,5, 1,3));
		/* start up the text fields for server name and port number */
		tfReceiverIP = new JTextField(this.recAddress);
		tfPortNo = new JTextField("" + this.portNo);
		tfPortNo.setHorizontalAlignment(SwingConstants.RIGHT);

		receiverPortPanel.add(new JLabel("Server Address:  "));
		receiverPortPanel.add(tfReceiverIP);
		receiverPortPanel.add(new JLabel("Port Number:  "));
		receiverPortPanel.add(tfPortNo);
		receiverPortPanel.add(new JLabel(""));
		/* put all of this in the north panel */
		northPanel.add(receiverPortPanel);

		/* The label and text field for communication */


		this.add(northPanel, BorderLayout.NORTH);

		/* 
		 * The CenterPanel where chat's are displayed and online users
		 * shown
		 */
		JPanel southPanel = new JPanel();

		/* the 3 buttons */
		sendfile = new JButton("Send");
		sendfile.addActionListener(this);
		receivefile = new JButton("Receive");
		receivefile.addActionListener(this);

		southPanel.add(sendfile);
		southPanel.add(receivefile);

		this.add(southPanel, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		/*
EXIT_ON_CLOSE
		*/
		this.setSize(600, 100);
		this.setVisible(true);
	}
	

	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == sendfile) {
			
			String server = tfReceiverIP.getText().trim();
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
			
			new Sender(server, port);

		} else if (o == receivefile) {
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
			
			new Receiver("localhost", port);
		} else {
			System.err.printf("This is weird\n");
		}
	}

    public static void main(String[] args)  {
		ShareFile sender = null;
		sender = new ShareFile("localhost", 8002);
	}
}
