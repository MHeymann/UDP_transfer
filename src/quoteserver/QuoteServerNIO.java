package quoteserver;

import java.io.*;

public class QuoteServerNIO {
	public static void main (String[] args)  throws IOException {
		System.out.printf("Starting the server with port 8002, opening file \"tale.txt\"\n");
		new QuoteServerThreadNIO("My Quote Server", "tale.txt", 8002).start();
	}
}
