package parameters;

/*
 * This class contains parameters that must ensure that sending and receiving
 * occurs in a coordinated fashion
 */

public class Parameters {
	public static final int BUFFER_SIZE = 512;
	public static final int SEQ_BYTES = 4;
	public static final int SIZE_BYTES = 4;
	public static final int DATA_BYTES = BUFFER_SIZE - (SEQ_BYTES + SIZE_BYTES);

	public static final int PORTS = 256;
	public static final int BURST_LENGTH = 256;
}

