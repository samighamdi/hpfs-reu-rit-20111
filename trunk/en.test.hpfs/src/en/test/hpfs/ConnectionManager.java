package en.test.hpfs;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;

public class ConnectionManager {

	private Socket client;
	private BufferedReader in;
	private PrintStream out;

	public ConnectionManager(String host, int port)
			throws UnknownHostException, IOException {
		client = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		out = new PrintStream(client.getOutputStream());
	}

	@Override
	protected void finalize() throws Throwable {
		client.close();
		super.finalize();
	}

	public void sendNumber(long num, int length) {
		out.println(new DecimalFormat("%0" + length + "d").format(num));
	}

	public void sendNumber(int num, int length) {
		sendNumber((long) num, length);
	}

	public void sendText(String text) {
		out.println(text);
	}

	public void sendBytes(byte[] bytes) throws IOException {
		client.getOutputStream().write(bytes, 0, bytes.length);
	}
	
	public long receiveNumber() throws IOException {
		try {
			return Long.parseLong(in.readLine());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String receiveText() throws IOException {
		return in.readLine();
	}

	public long receiveLong() throws IOException {
		try {
			return Long.parseLong(in.readLine());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public byte[] receiveBytes(int length) throws IOException {
			byte[] imageBytes = new byte[length];
			new DataInputStream(client.getInputStream()).readFully(imageBytes);
			return imageBytes;
	}
}
