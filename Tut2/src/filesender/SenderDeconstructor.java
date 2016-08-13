package filesender;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

public class SenderDeconstructor implements Runnable {
	private String fileLocation = null;
	private String IP_Address = null;
	private Sender sender = null;
	private int port = -1;

	private SocketChannel socketChannel = null;
	private DatagramChannel datagramChannel = null;
	private Selector selector = null;

	public SenderDeconstructor(String fileLocation, String IP_Address, int port, Sender sender) 
	{
		this.fileLocation = fileLocation;
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

	}

	public boolean connect() {
		try {
			this.selector = Selector.open();
			this.datagramChannel = DatagramChannel.open();
			this.datagramChannel.configureBlocking(false);
			this.socketChannel = SocketChannel.open();
			this.socketChannel.configureBlocking(false);
			this.socketChannel.connect(new InetSocketAddress(this.IP_Address, this.port));
			while (!this.socketChannel.finishConnect());
		} catch (IOException e) {
			sender.appendTCP("IOException: failed to create SocketChannel\n");
			return false;
		}
		sender.appendTCP("Set up TCP connection\n");

		DatagramSocket dSocket = datagramChannel.socket();
		try {
			dSocket.bind(new InetSocketAddress(this.IP_Address, this.port));
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
			this.datagramChannel.register(selector, SelectionKey.OP_READ);
			this.socketChannel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			System.out.printf("Closed Channel Exception\n");
			e.printStackTrace();
		}

		return true;
	}
}
