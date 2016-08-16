package quoteclient;

//Receiver 


import java.io.*;
import java.net.*;
import java.util.*;

public class QuoteClient {
	public static void main(String[] args) throws IOException {
		DatagramSocket socket = null;
		InetAddress address = null;
		byte[] buf = null;
		DatagramPacket packet = null;
		int port = 8002;
		String hostName = null;

		if (args.length == 0) {
			System.out.printf("Usage: java QuoteClient <hostname> [<port>]\n");
			return;
		} 
		hostName = args[0];
		if (args.length > 1) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.out.printf("Please run with valid port number\n");
				e.printStackTrace();
			}
		}

		/* get a datagram socket */
		socket = new DatagramSocket();

		/* send request */
		buf = new byte[256];
		address = InetAddress.getByName(hostName);
		packet = new DatagramPacket (buf, buf.length, address, port);
		socket.send(packet);

		/* get response */
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);

		/* display response */
		String received = new String(packet.getData(), 0, packet.getLength());
		System.out.printf("Quote of the Moment: %s\n", received);

		socket.close();
	}
}
