package tcpreceiver;

import java.net.*;
import java.nio.channels.*;
import java.nio.*;
import java.io.FileOutputStream;
import java.util.*;
import java.io.IOException;
import javax.swing.JOptionPane;
/*
import java.lang.System;
*/

import packet.*;
import parameters.*;

public class ReceiverReconstructor implements Runnable {
	private Receiver receiver = null;
	private int port = -1;
	private ServerSocketChannel serverSocketChannel = null;
	private Selector selector = null;
	private FileChannel fcout = null;
	private String filePath = "hardcodefile";
	private long startTime = -1;
	private long endTime = -1;
	private int expectedReads = -1;
	private int currentRead = 0;

	public ReceiverReconstructor(Receiver receiver, int port) {
		InetSocketAddress address = null;
		ServerSocket serverSocket = null;
		SelectionKey key = null;
		this.currentRead = 0;
		int i;

		this.receiver = receiver;
		this.port = port;
		try {
			selector = Selector.open();
			/* open a listener for the given port, register with selector */
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);

			serverSocket = serverSocketChannel.socket();
			serverSocket.bind(new InetSocketAddress(this.port));
			key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			this.receiver.appendTCP("Listening on " + this.port + "\n");
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			go();
		} catch (Exception e) {
			this.receiver.appendTCP("Some problem in Receive Listener\n");
			e.printStackTrace();
		}
	}

	public void go() throws Exception
	{
		SocketChannel sChannel = null;
		SelectionKey key = null;
		Set<SelectionKey> selectedKeys = null;
		Iterator<SelectionKey> it = null;
		ByteBuffer buffer = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		int n;

		while (true) {
			n = selector.select();
			
			selectedKeys = null;
			selectedKeys = selector.selectedKeys();
			it = null;
			it = selectedKeys.iterator();

			while (it.hasNext()) {
				key = it.next();

				if ((key.readyOps() & SelectionKey.OP_ACCEPT) 
					== SelectionKey.OP_ACCEPT) {
					serverSocketChannel = null;
					serverSocketChannel = (ServerSocketChannel)key.channel();

					sChannel = null;
					sChannel = serverSocketChannel.accept();
					sChannel.configureBlocking(false);
					sChannel.register(selector, SelectionKey.OP_READ);
					
					this.receiver.appendTCP("New TCP connection from " + sChannel.toString() + "\n");

					try {
						Thread.sleep(400);
					} catch (Exception e) {
						e.printStackTrace();
					}

					buffer.clear();
					int r = sChannel.read(buffer);
					System.out.printf("Read %d bytes\n", r);
	
					if (r == -1) {
						System.out.printf("Connection broke down\n");
						System.exit(1);
					}
					else {
						buffer.flip();
						this.expectedReads = buffer.getInt();
						String tcpmessage = "" + expectedReads + " packets expected\n";
						this.receiver.appendTCP(tcpmessage);
						System.out.printf("%s", tcpmessage);
						buffer.rewind();
						sChannel.write(buffer);
						buffer.clear();
					}


					try {
						this.filePath = (String)JOptionPane.showInputDialog(
							receiver,
							"Please give the file name you prefer",
							"File Name",
							JOptionPane.PLAIN_MESSAGE
                    	);
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }

					//If a string was returned, say so.
					if ((this.filePath != null) && (this.filePath.length() > 0)) {
					} else {
						this.filePath = "myFile";
					}

					this.startTime = System.currentTimeMillis();

					FileOutputStream fout = new FileOutputStream(this.filePath);
					this.fcout = fout.getChannel();
					it.remove();
				} else if ((key.readyOps() & SelectionKey.OP_READ)
						== SelectionKey.OP_READ) {

					sChannel = (SocketChannel)key.channel();
					buffer.clear();
					int r = sChannel.read(buffer);
					if (r == -1) {
						String tcpmessage = "TCP connection broke down!\n";
						this.receiver.appendTCP(tcpmessage);
						System.out.printf("%s", tcpmessage);
						sChannel.close();
						System.exit(1);
					} else {
						buffer.flip();
						try {
							fcout.write(buffer);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					double percentage = ((((10000L * (1 + currentRead)) / this.expectedReads) + 0.0) / 100);

					this.receiver.appendTCP("" + percentage + "%\n");
					if (percentage >= 99.99) {
						this.endTime = System.currentTimeMillis();
						double time = ((this.endTime - this.startTime) / 100 + 0.1) / 10;
						receiver.appendTCP("Time taken in seconds: " + time + "\n");
						System.out.println("Time taken in seconds: " + time);
					}
					currentRead++;
					it.remove();
				}
			}
		}
	}

}
