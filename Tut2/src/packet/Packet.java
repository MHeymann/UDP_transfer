package packet;

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
		return that.sequenceNumber - this.sequenceNumber;
	}

}
