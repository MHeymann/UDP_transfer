package quoteserver;

import java.io.*;

public class QuoteServer {
	public static void main (String[] args)  throws IOException {
		System.out.printf("Starting the server with port 8002, opening file \"tale.txt\"\n");
		new QuoteServerThread("My Quote Server", "tale.txt", 8002).start();
	}
}
