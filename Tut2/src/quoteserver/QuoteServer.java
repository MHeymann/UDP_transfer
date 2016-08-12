package quoteserver;

import java.io.*;

public class QuoteServer {
	public static void main (String[] args)  throws IOException {
		System.out.printf("Starting the server with port 8002, opening file \"one-liners.txt\"\n");
		new QuoteServerThread("My Quote Server", "one-liners.txt", 8002).start();
	}
}
