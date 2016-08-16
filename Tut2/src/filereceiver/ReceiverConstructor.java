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
		this.receiver = receiver;
		this.sChannel = sChannel;
		int port = receiver.getPort();
		try {
			this.dChannel = DatagramChannel.open();
			this.dChannel.configureBlocking(false);
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
		}

		try {
			this.dChannel.register(this.selector, SelectionKey.OP_READ);
			this.sChannel.register(this.selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			System.out.printf("Closed Channel Exception\n");
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
			e.printStackTrace();
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
			System.out.println(this.sChannel);
			n = this.selector.select();

			selectedKeys = null;	
			selectedKeys = this.selector.selectedKeys();
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
					this.receiver.appendUDP("Reading from dChannel\n");
					buffer.clear();
					dChannel.receive(buffer);
					buffer.flip();
					int seqNo = buffer.getInt();
					int size = buffer.getInt();
					System.out.printf("seq no %d, size %d, remaining %d\n", seqNo, size, buffer.remaining());
					byte[] data = new byte[size];
					buffer.get(data, 0, size);
					Packet packet = new Packet(seqNo, size, data);

					this.pq.add(packet);


				} else if (key.channel() == sChannel) {
					System.out.printf("posting to gui\n");
					this.receiver.appendTCP("Reading from sChannel\n");
					System.out.printf("Reading from sChannel\n");
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
