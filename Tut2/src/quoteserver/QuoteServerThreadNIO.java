package quoteserver;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.util.*;


public class QuoteServerThreadNIO extends Thread {

	protected boolean moreQuotes = true;
	private int ports[] = null;
	private BufferedReader in = null;
	/*
	private ByteBuffer echoBuffer = null;
	*/

	public QuoteServerThreadNIO() throws IOException 
	{
		super("QuoteServerThread");
		this.ports = new int[1];
		this.ports[0] = 8002;
		openFile("tale.txt");
	}

	public QuoteServerThreadNIO(String name, String fileName, int port) throws IOException 
	{
		super(name);
		this.ports = new int[1];
		this.ports[0] = port;
		openFile(fileName);
	}
	
	public QuoteServerThreadNIO(String name, String fileName, int port[]) 
	{
		super(name);
		this.ports = port;
		openFile(fileName);
	}

	public boolean openFile(String fileName) 
	{
		try {
			this.in = new BufferedReader(new FileReader(fileName));
			File file = new File("tale.txt");
			if(file.exists()){
				double bytes = file.length();
				System.out.println("bytes : " + bytes);
			}else{
					 System.out.println("File does not exist!");
			}
		} catch (FileNotFoundException e) {
			System.err.printf("Could not open quote file. Serving time instead\n");
			this.in = null;
		}

		return true;
	}

	public void run()
	{
		try {
			go();
		} catch (Exception e) {
			System.out.printf("Exception on running server");
			e.printStackTrace();
			return;
		}
	}

	public void go() throws IOException  {
		int i, num;
		Selector selector = null;
		/*
		DatagramSocket socket = null;
		socket = new DatagramSocket(port);
		*/
		DatagramChannel dChannel = null;
		DatagramSocket dSocket = null;
		SocketAddress address = null;
		SelectionKey key = null;
		Set<SelectionKey> selectedKeys = null;
		Iterator<SelectionKey> it = null;
		DatagramPacket packet = null;
		byte[] buf = null;

		/* Creating a new selctor */
		selector = Selector.open(); 

		/* open a listener for each port and register with selector */
		for (i = 0; i < this.ports.length; i++) {
			dChannel = null;
			dSocket = null;
			address = null;
			key = null;

			dChannel = DatagramChannel.open();
			dChannel.configureBlocking(false);

			dSocket = dChannel.socket();
			address = new InetSocketAddress(ports[i]);
			dSocket.bind(address);

			//cation
			key = dChannel.register(selector, SelectionKey.OP_READ);

			System.out.printf("Going to listen on %d\n", ports[i]);
		}


		System.out.printf("Server Running\n");
		
		while (moreQuotes) {
			num = selector.select();
			selectedKeys = null;
			selectedKeys = selector.selectedKeys();
			it = null;
			it = selectedKeys.iterator();

			while (it.hasNext()) {
				key = it.next();

				if ((key.readyOps() & SelectionKey.OP_READ) 
						== SelectionKey.OP_READ) {
					
					try {
						buf = new byte[256];
						ByteBuffer bb = ByteBuffer.allocate(256);

						dChannel = (DatagramChannel)key.channel();
						/* receive request */
						/*
						packet = new DatagramPacket(buf, buf.length);
						*/
						
						address = null;
						address = dChannel.receive(bb);
						buf = bb.array();
						System.out.printf("Received datagram from %s\n", address.toString());
		
						/* figure out response */
						String dString = null;
						if (in == null) {
							dString = new Date().toString();
							System.out.printf("Sending date\n");
						} else {
							dString = getNextQuote(in);
							System.out.printf("Sending quote\n");
						}
						buf = dString.getBytes();
						bb = ByteBuffer.wrap(buf);

						/* send the response to the client at "address" 
						 * and "port" */
						dChannel.send(bb, address);
						System.out.printf("\n");
						/*StringBuilder line = new StringBuilder("[");
						for (i = 0; i < 100; i++) {
							line.append(" ");
						}
						line.append("]");
						for (i = 0; i < 100; i++) {
							line.setCharAt(i + 1, '|');
							System.out.printf("\r%s", line.toString());

						}*/
						System.out.printf("\nSent\n");
					} catch (IOException e) {
						System.err.printf("Something weird in moreQuotes loop\n");
						e.printStackTrace();
						moreQuotes = false;
					}


				} else if ((key.readyOps() & SelectionKey.OP_ACCEPT) 
						== SelectionKey.OP_ACCEPT) {
					System.out.printf("Well this was unexpected\n");
				}
			}

		}
		/*
		socket.close();
		*/
	}

	protected String getNextQuote(BufferedReader in) {
		String returnValue = null;
		try {
			if ((returnValue = in.readLine()) == null) {
				in.close();
				moreQuotes = false;
				returnValue = "No more quotes. Goodbye";
			}
		} catch (IOException e) {
			returnValue = "IOException occured in server.";
		}
		return returnValue;
	}
}
