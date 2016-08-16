package packet;

import java.nio.channels.*;
import java.nio.*;
import java.io.*;
import java.net.*;
import parameters.*;
import java.io.IOException;

public class Packet implements Comparable<Packet> {
	private int sequenceNumber = -1;
	private int size = -1;
	private byte[] data = null;

	public Packet (int sequenceNumber, int size, byte[] data) {
		this.sequenceNumber = sequenceNumber;
		this.size = size;
		this.data = data;
	}

	public int getSeqNum() {
		return this.sequenceNumber;
	}

	public long getSize() {
		return this.size;
	}

	public byte[] getData() {
		return this.data;
	}

	@Override
	public int compareTo(Packet that) {
		return this.sequenceNumber - that.sequenceNumber;
	}

	public void sendPacket(DatagramChannel channel, InetSocketAddress address) {
		ByteBuffer sendBuff = ByteBuffer.allocate(Parameters.BUFFER_SIZE);
		sendBuff.clear();
		sendBuff.putInt(this.sequenceNumber);	
		sendBuff.putInt(this.size);	
		sendBuff.put(this.data, 0, size);	
		sendBuff.flip();
		try {
			channel.send(sendBuff, address);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
