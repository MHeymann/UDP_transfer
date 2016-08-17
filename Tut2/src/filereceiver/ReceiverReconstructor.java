package filereceiver;

import java.net.*;
import java.nio.channels.*;
import java.nio.*;
import java.io.FileOutputStream;
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
	private FileChannel fcout = null;
	private String filePath = "hardcodefile";
	private int expectedPackets = -1;

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
		boolean receiving = false;

		while (true) {
			n = selector.select(4);
			if (n == 0) {
				if (pingback) {
					this.pingBack();
					pingback = false;
				}
				continue;
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


					try {
						Thread.sleep(400);
					} catch (Exception e) {
						e.printStackTrace();
					}

					buffer.clear();
					int r = this.sChannel.read(buffer);
					System.out.printf("%d bytes read\n", r);
					if (r == -1) {
						String tcpmessage = "TCP connection broke down!\n";
						this.receiver.appendTCP(tcpmessage);
						System.out.printf("%s", tcpmessage);
						this.sChannel.close();
						System.exit(1);
					} else {
						buffer.flip();
						this.expectedPackets = buffer.getInt();
						String tcpmessage = "" + expectedPackets + " packets expected\n";
						this.receiver.appendTCP(tcpmessage);
						System.out.printf("%s", tcpmessage);
						buffer.rewind();
						sChannel.write(buffer);
						buffer.clear();
					}
					//xxxx
					receiving = true;


					//yyyy

					FileOutputStream fout = new FileOutputStream(this.filePath);
					this.fcout = fout.getChannel();
					it.remove();
				} else if ((key.readyOps() & SelectionKey.OP_READ)
						== SelectionKey.OP_READ) {

					if (key.channel() instanceof DatagramChannel) {
						dChannel = (DatagramChannel)key.channel();
						buffer.clear();
						dChannel.receive(buffer);
						buffer.flip();
						int seqNo = buffer.getInt();
						int size = buffer.getInt();
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
						if (receiving) {
							buffer.clear();
							int r = this.sChannel.read(buffer);
							if (r == -1) {
								String tcpmessage = "TCP connection broke down!\n";
								this.receiver.appendTCP(tcpmessage);
								System.out.printf("%s", tcpmessage);
								this.sChannel.close();
								System.exit(1);
							} else {
								buffer.flip();
								r = buffer.getInt();
								expectLow = r;
								r = buffer.getInt();
								expectHigh = r;
								if (r == -1) {
									System.exit(0);
								} 
								//System.out.printf("Ping registered\n");
								pingback = true;
							}
						} else {
							//xxxxxx
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
		ByteBuffer intBuffer = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		ByteBuffer countBuffer = ByteBuffer.allocate(4);
		ByteBuffer buffers[] = new ByteBuffer[2];
		int i;
		int countDrops = 0;
		intBuffer.clear();
		countBuffer.clear();
		PriorityQueue<Packet> tempq = new PriorityQueue<Packet>();
		//System.out.printf("PingBack!!\n");
	
		Packet p;
		i = expectLow;
		for (i = expectLow; i < expectHigh + 1; i++) {
			if ((p = this.pq.poll()) == null) {
				countDrops++;
				intBuffer.putInt(i);
				continue;
			}
			while (i < p.getSeqNum()) {
				countDrops++;
				intBuffer.putInt(i);
				i++;
			}
			tempq.add(p);
		}

		countBuffer.putInt(countDrops);
		if (countDrops == 0) {
			this.receiver.appendTCP("" + ((((10000 * (1 + expectHigh)) / this.expectedPackets) + 0.0) / 100) + "%\n");
			writeToFile(tempq);
			intBuffer.putInt(-1);
		} else {
			this.pq = tempq;
		}


		countBuffer.flip();
		intBuffer.flip();
		buffers[0] = countBuffer;
		buffers[1] = intBuffer;
		try {
			this.sChannel.write(buffers);
		} catch (IOException e) {
			System.out.printf("IOException\n");
			e.printStackTrace();
		}

		//System.out.printf("pq size is %d\n", this.pq.size());
	}

	public void writeToFile(PriorityQueue<Packet> q) {
		Packet p = null;
		ByteBuffer buffer = ByteBuffer.allocate(Parameters.DATA_BYTES);
		while ((p = q.poll()) != null) {
			buffer.clear();
			buffer.put(p.getData());
			buffer.flip();
			try {
				fcout.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
