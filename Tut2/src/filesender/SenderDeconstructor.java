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
		FileInputStream fin = null;
		FileChannel fcin = fin.getChannel();

		try {
			fin =new FileInputStream(this.fileLocation);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int r = 0;
		int sequenceNo = 0;

		for (sequenceNo = 0; true; sequenceNo++) {
			sendBuff.clear();
			readBuff.clear();

			try {
				r = fcin.read(readBuff);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(r == -1) {
				break;
			}
			
			sendBuff.putInt(sequenceNo);
			sendBuff.putInt(r);
			sendBuff.put(readBuff);

			
			sendBuff.flip();
			/* probably unnecessary */
			readBuff.flip();

			try {
				this.datagramChannel.send(sendBuff, this.address);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		sequenceNo--;
		sendBuff.clear();
		sendBuff.flip();
		
		
		try {
			this.socketChannel.write(sendBuff);
			selector.select();
			r = socketChannel.read(readBuff);
			r = readBuff.getInt();
			if(r == sequenceNo){
				System.out.println("Seems to be working");
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean connect() {
		this.address = new InetSocketAddress(this.IP_Address, this.port);
		try {
			this.selector = Selector.open();
			this.datagramChannel = DatagramChannel.open();
			this.datagramChannel.configureBlocking(false);
			this.socketChannel = SocketChannel.open();
			this.socketChannel.configureBlocking(false);
			this.socketChannel.connect(this.address);
			while (!this.socketChannel.finishConnect());
		} catch (IOException e) {
			sender.appendTCP("IOException: failed to create SocketChannel\n");
			return false;
		}
		sender.appendTCP("Set up TCP connection\n");

		DatagramSocket dSocket = datagramChannel.socket();
		/* TODO: since we arent receiving back, is it necessary to bind? */
		try {
			dSocket.bind(this.address);
		} catch (SocketException e) {
			System.out.printf("Socket Exception\n");
			e.printStackTrace();
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
