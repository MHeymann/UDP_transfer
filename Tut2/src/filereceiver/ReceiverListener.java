package filereceiver;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class ReceiverListener implements Runnable {
	private int port = -1;
	private Receiver receiver = null;

	public ReceiverListener(Receiver receiver, int port) {
		this.port = port;
		this.receiver = receiver;
	}

	public void run() {
		try {
			go();
		} catch (Exception e) {
			this.receiver.appendTCP("Some problem in Receive Listener");
		}
	}

	public void go() throws Exception
	{
		int num;
		ServerSocketChannel serverSocketChannel = null;
		Selector selector = null;
		ServerSocket serverSocket = null;
		InetSocketAddress address = null;
		SelectionKey key = null;
		Set<SelectionKey> selectedKeys = null;
		Iterator<SelectionKey> it = null;
		SocketChannel socketChannel = null;
		SelectionKey newKey = null;
		

		selector = Selector.open();

		/* open a listener for the given port, register with selector */
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);

		serverSocket = serverSocketChannel.socket();
		address = new InetSocketAddress(this.port);
		serverSocket.bind(address);
		key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		this.receiver.appendTCP("Listening on " + this.port);

		while (true) {
			num = selector.select();
			
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

					socketChannel = null;
					socketChannel = serverSocketChannel.accept();
					socketChannel.configureBlocking(false);
					
					/* register on selector */
					newKey = null;
					newKey = socketChannel.register(selector, SelectionKey.OP_READ);
					it.remove();

					this.receiver.appendTCP("New TCP connection from " + socketChannel.toString());
				} else if ((key.readyOps() & SelectionKey.OP_ACCEPT)
						== SelectionKey.OP_READ) {
					socketChannel = null;
					socketChannel = (SocketChannel)key.channel();
					/* 
					 * Do things to let the whole process begin
					 */
					it.remove();
				}
			}
		}
	}

}
