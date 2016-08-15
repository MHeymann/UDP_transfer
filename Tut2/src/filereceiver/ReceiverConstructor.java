package filereceiver;
import java.nio.*;
import packet.*;
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
	private PriorityQueue<Packet> pq = null;

	public ReceiverConstructor(SocketChannel sChannel, Receiver receiver) {
		int port = receiver.getPort();
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
			dSocket.bind(new InetSocketAddress(port + 1));
		} catch (SocketException e) {
			System.out.printf("Socket Exception\n");
			e.printStackTrace();
			/*
		} catch (IOException e) {
			System.out.printf("IOException\n");
			e.printStackTrace();
			*/
		}

		try {
			this.dChannel.configureBlocking(false);
			/*
			this.sChannel.configureBlocking(false);
			*/
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

		this.pq = new PriorityQueue<Packet>();
	}

	public void run() {
		try {
			go();
		} catch (Exception e) {
			receiver.appendTCP("Some exception while receiving file\n");
		}
	}

	public void go() throws Exception 
	{
		Set<SelectionKey> selectedKeys = null;
		Iterator<SelectionKey> it = null;
		SelectionKey key = null;
		ByteBuffer buffer = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		int n;
		/*
		InetSocketAddress address = null;
		*/

		System.out.println("Let's go while(d)!");
		while (true) {
			this.selector.select();
			System.out.printf("selected\n");

			selectedKeys = null;	
			selectedKeys = selector.selectedKeys();
			it = null;
			it = selectedKeys.iterator();

			System.out.printf("OO look, another while!\n");
			while(it.hasNext()) {
				key = it.next();
				System.out.printf
				if ((key.readyOps() & SelectionKey.OP_READ) 
						!= SelectionKey.OP_READ) {
					System.out.printf("this shouldn't happen\n");
					it.remove();
					continue;
				}
				if (key.channel() == dChannel) {
					this.receiver.appendUDP("Reading from dChannel\n");
					buffer.clear();
					dChannel.receive(buffer);
					buffer.flip();
					int seqNo = buffer.getInt();
					int size = buffer.getInt();
					System.out.printf("seq no %d, size %d\n", seqNo, size);
					byte[] data = null;
					buffer.get(data, 0, (int)size);
					Packet packet = new Packet(seqNo, size, data);

					this.pq.add(packet);


				} else if (key.channel() == sChannel) {
					this.receiver.appendTCP("Reading from sChannel\n");
					buffer.clear();
					int r = this.sChannel.read(buffer);
					System.out.printf("Read %d bytes\n", r);
					buffer.clear();
					buffer.putInt(this.pq.size());
					buffer.flip();
					this.sChannel.write(buffer);
					System.out.printf("pq size is %d\n", this.pq.size());
					
				} else {
					System.err.printf("well, this is weird\n");
				}
				it.remove();	
			}
		}
	}
}
