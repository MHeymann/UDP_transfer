package filereceiver;

import java.net.*;

import java.nio.channels.*;
import java.util.*;

public class ReceiverListener implements Runnable {
	private Receiver receiver = null;
	private int port = -1;

	public ReceiverListener(Receiver receiver, int port) {
		this.receiver = receiver;
		this.port = port;
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
		ServerSocketChannel serverSocketChannel = null;
		SocketChannel socketChannel = null;
		Selector selector = null;
		ServerSocket serverSocket = null;
		InetSocketAddress address = null;
		SelectionKey key = null;
		Set<SelectionKey> selectedKeys = null;
		Iterator<SelectionKey> it = null;

		

		selector = Selector.open();

		/* open a listener for the given port, register with selector */
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);

		serverSocket = serverSocketChannel.socket();
		address = new InetSocketAddress(this.port);
		serverSocket.bind(address);
		key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		this.receiver.appendTCP("Listening on " + this.port + "\n");

		while (true) {
		selector.select();
			
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
					
					(new Thread(new ReceiverConstructor(socketChannel, this.receiver))).start();
					this.receiver.appendTCP("New TCP connection from " + socketChannel.toString() + "\n");
					it.remove();
				} else if ((key.readyOps() & SelectionKey.OP_READ)
						== SelectionKey.OP_READ) {
					this.receiver.appendTCP("This is thoroughly weird\n");
					it.remove();
				}
			}
		}
	}

}
