package filereceiver;

import java.net.*;
import java.nio.channels.*;
import java.nio.*;
import java.util.*;
import java.io.IOException;

import packet.*;
import parameters.*;

public class ReceiverReconstructor implements Runnable {
	private Receiver receiver = null;
	private int port = -1;
	private SocketChannel sChannel = null;
	private PriorityQueue<Packet> pq = null;
	private ServerSocketChannel serverSocketChannel = null;
	private Selector selector = null;
	private int expectLow = -1;
	private int expectHigh = -1;

	public ReceiverReconstructor(Receiver receiver, int port) {
		InetSocketAddress address = null;
		ServerSocket serverSocket = null;
		SelectionKey key = null;
		DatagramChannel dChannel = null;
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
			/* NB: this must be after binding the serverSocketChannel address */
			/*
			this.port++;
			*/
	
			/* set up the udp channels */
			receiver.appendUDP("Setting up UDP channel for receiving file\n");
			for (i = 0; i < Parameters.PORTS; i++) {
				dChannel = DatagramChannel.open();
				dChannel.configureBlocking(false);
				DatagramSocket dSocket = dChannel.socket();
				dSocket.bind(new InetSocketAddress(this.port + i));
				System.out.printf("registered port %d\n", (i + this.port));
				dChannel.register(selector, SelectionKey.OP_READ);
			}
			receiver.appendUDP("Set up UDP channels for receiving file\n");
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.pq = new PriorityQueue<Packet>();
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
		SelectionKey key = null;
		Set<SelectionKey> selectedKeys = null;
		Iterator<SelectionKey> it = null;
		DatagramChannel dChannel = null;
		ByteBuffer buffer = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		int n;
		boolean pingback = false;

		while (true) {
			n = selector.select(100);
			if (n == 0) {
				if (pingback) {
					this.pingBack();
					pingback = false;
				}
			}
			
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
					it.remove();
				} else if ((key.readyOps() & SelectionKey.OP_READ)
						== SelectionKey.OP_READ) {

					if (key.channel() instanceof DatagramChannel) {
						dChannel = (DatagramChannel)key.channel();
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
						if (pingback) {
							if (pq.size() == (expectHigh - expectLow + 1)) {
								this.pingBack();
								pingback = false;
							}
						}


					} else if (key.channel() == sChannel) {
						this.receiver.appendTCP("Reading from sChannel\n");
						System.out.printf("Reading from sChannel\n");
						buffer.clear();
						int r = this.sChannel.read(buffer);
						if (r == -1) {
							String tcpmessage = "TCP connection broke down!\n";
							this.receiver.appendTCP(tcpmessage);
							System.out.printf("%s", tcpmessage);
							this.sChannel.close();
						} else {
							buffer.flip();
							r = buffer.getInt();
							expectLow = r;
							r = buffer.getInt();
							expectHigh = r;
							pingback = true;
						}
					} else {
						System.err.printf("well, this is weird\n");
					}

					it.remove();
				}
			}
		}
	}

	public void pingBack() {
		ByteBuffer buffer = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		int i;
		buffer.clear();
		buffer.putInt(this.pq.size());
		buffer.flip();
		try {
			this.sChannel.write(buffer);
		} catch (IOException e) {
			System.out.printf("IOException\n");
			e.printStackTrace();
		}
		Packet p;
		i = expectLow;
		while ((p = this.pq.poll()) != null){
			while (i < p.getSeqNum()) {
				System.out.printf("dropped %d\n", i);
				i++;
			}
			System.out.printf("seqno: %d\n", p.getSeqNum());
			i++;
		}

		System.out.printf("pq size is %d\n", this.pq.size());
	}

}
