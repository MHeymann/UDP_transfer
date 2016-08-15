package filereceiver;
import java.nio.*;
import java.nio.channels.*;
import java.io.IOException;
import java.util.*;
import java.net.*;
import parameters.*;

public class ReceiverConstructor implements Runnable {
	private Receiver receiver = null;
	private SocketChannel sChannel = null;
	private DatagramChannel dChannel = null;
	private Selector selector = null;

	public ReceiverConstructor(SocketChannel sChannel, Receiver receiver) {
		this.receiver = receiver;
		this.sChannel = sChannel;
		try {
			this.dChannel = DatagramChannel.open();
			this.selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}

		receiver.appendUDP("Setting up UDP channel for receiving file\n");
		
		DatagramSocket dSocket = dChannel.socket();
		try {
			dSocket.bind(sChannel.getRemoteAddress());
		} catch (SocketException e) {
			System.out.printf("Socket Exception\n");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.printf("IOException\n");
			e.printStackTrace();
		}

		try {
			this.dChannel.configureBlocking(false);
			this.dChannel.register(selector, SelectionKey.OP_READ);
			this.sChannel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			System.out.printf("Closed Channel Exception\n");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.printf("IOException\n");
			e.printStackTrace();
		}

		receiver.appendUDP("Set up UDP channel for receiving file\n");
	}

	public void run() {
		try {
			go();
		} catch (Exception e) {
			receiver.appendTCP("Some exeption while receiving file\n");
		}
	}

	public void go() throws Exception 
	{
		Set<SelectionKey> selectedKeys = null;
		Iterator<SelectionKey> it = null;
		SelectionKey key = null;
		ByteBuffer buffer = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		/*
		InetSocketAddress address = null;
		*/

		while (true) {
			this.selector.select();

			selectedKeys = null;	
			selectedKeys = selector.selectedKeys();
			it = null;
			it = selectedKeys.iterator();

			while(it.hasNext()) {
				key = it.next();
				if ((key.readyOps() & SelectionKey.OP_READ) 
						!= SelectionKey.OP_READ) {
					System.out.printf("this shouldn't happen\n");
					it.remove();
					continue;
				}
				if (key.channel() == dChannel) {
					dChannel.receive(buffer);
					buffer.flip();
					int seqNo = buffer.getInt();
					long size = buffer.getInt();
					byte[] data = null;
					buffer.get(data, 0, (int)size);
					Packet packet = new Packet(bla bla bla);
					this.receiver.appendUDP("Reading from dChannel\n");
				} else if (key.channel() == sChannel) {
					this.receiver.appendTCP("Reading from sChannel\n");
				} else {
					System.err.printf("well, this is weird\n");
				}
				it.remove();	
			}
		}
	}
}
