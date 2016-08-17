package tcpsender;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import parameters.*;
import java.util.HashMap;
import java.util.Arrays;
import packet.Packet;

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
	private HashMap<Integer, Packet> hMap = null;

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
		this.hMap = null;;
	}

	public void run() {
		go();
	}

	public void go() {
		ByteBuffer buffer = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		FileInputStream fin;
		FileChannel fcin;
		int r = 0, r2 = 0;
		int i;
		
		try{
			fin = new FileInputStream(this.fileLocation);	
			fcin = fin.getChannel();
		}
		catch (FileNotFoundException e){
			return;
		}

		while(true) {
			buffer.clear();

			try {
				r = fcin.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (r == -1) {
				break;
			}

			buffer.flip();
			try {
				this.socketChannel.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}


		}
		
		try {
			fin.close();
			this.socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("Done sending file\n");
	}

	public boolean connect() {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		try {
			this.selector = Selector.open();
			this.socketChannel = SocketChannel.open();
			this.socketChannel.configureBlocking(false);
			this.address = new InetSocketAddress(this.IP_Address, this.port);
			this.socketChannel.connect(this.address);
			while (!this.socketChannel.finishConnect());
			this.sender.appendTCP("Set up TCP connection\n");
			this.socketChannel.register(selector, SelectionKey.OP_READ);
			buffer.clear();
			buffer.putInt((this.fileSize / (Parameters.BUFFER_SIZE) + 1));
			buffer.flip();
			this.socketChannel.write(buffer);
			selector.select();
			buffer.clear();
			socketChannel.read(buffer);
		} catch (IOException e) {
			sender.appendTCP("IOException: failed to create SocketChannel\n");
			return false;
		}
		return true;
	}


}
