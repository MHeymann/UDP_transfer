package filesender;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import parameters.*;

public class SenderDeconstructor implements Runnable {
	private String fileLocation = null;
	private int fileSize = -1;
	private String IP_Address = null;
	private Sender sender = null;
	private int port = -1;

	private SocketChannel socketChannel = null;
	private DatagramChannel datagramChannel = null;
	private InetSocketAddress address = null;
	private Selector selector = null;

	public SenderDeconstructor(String fileLocation, int fileSize, String IP_Address, int port, Sender sender) 
	{
		this.fileLocation = fileLocation;
		this.fileSize = fileSize;
		this.IP_Address = IP_Address;
		this.sender = sender;
		this.port = port;
		this.socketChannel = null;
		this.datagramChannel = null;
		this.selector = null;
	}

	public void run() {
		go();
	}

	public void go() {
		ByteBuffer sendBuff = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		ByteBuffer readBuff = ByteBuffer.allocate(Parameters.DATA_BYTES);
		FileInputStream fin;
		FileChannel fcin;
		int r = 0;
		int sequenceNo = 0;
		
		try{
			fin = new FileInputStream(this.fileLocation);	
			fcin = fin.getChannel();
		}
		catch (FileNotFoundException e){
			return;
		}
		

		for (sequenceNo = 0; true; sequenceNo++) {
			sendBuff.clear();
			readBuff.clear();

			try {
				r = fcin.read(readBuff);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(r == -1) {
				System.out.printf("Done reading\n");
				break;
			}
			
			sendBuff.putInt(sequenceNo);
			sendBuff.putInt(r);
			readBuff.flip();
			sendBuff.put(readBuff);

			
			sendBuff.flip();

			try {
				this.datagramChannel.send(sendBuff, new InetSocketAddress(this.IP_Address, this.port + 1));
				System.out.printf("sent packet %d\n", sequenceNo);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		sendBuff.clear();
		sendBuff.putInt(1);
		sendBuff.flip();
		
		try {
			this.sender.appendTCP("Ping\n");
			this.socketChannel.write(sendBuff);
			System.out.println(socketChannel);
			selector.select();
			readBuff.clear();
			r = socketChannel.read(readBuff);
			if (r == -1) {
				System.out.printf("receiver TCP disconnected\n");
				this.sender.appendTCP("receiver TCP disconnected\n");
				this.socketChannel.close();
			} else {
				System.out.printf("read %d bytes\n", r);
				readBuff.flip();
				r = readBuff.getInt();
				System.out.printf("read: %d sent: %d\n", r, sequenceNo);
				if(r == sequenceNo){
					System.out.println("Seems to be working");
					this.sender.appendTCP("Seems to be working");
				} else {
					System.out.printf("Apparently not working\n");
				}
			}
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("Done sending file\n");
		this.sender.appendTCP("Set up TCP connection\n");
	}

	public boolean connect() {
		try {
			this.selector = Selector.open();
			this.datagramChannel = DatagramChannel.open();
			this.socketChannel = SocketChannel.open();
			this.socketChannel.configureBlocking(false);
			this.datagramChannel.configureBlocking(false);
			this.address = new InetSocketAddress(this.IP_Address, this.port);
			this.socketChannel.connect(this.address);
			while (!this.socketChannel.finishConnect());
			this.sender.appendTCP("Set up TCP connection\n");
		} catch (IOException e) {
			sender.appendTCP("IOException: failed to create SocketChannel\n");
			return false;
		}

		try {
			this.datagramChannel.register(selector, SelectionKey.OP_READ);
			this.socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			System.out.printf("Closed Channel Exception\n");
			e.printStackTrace();
		}
		return true;
	}
}
