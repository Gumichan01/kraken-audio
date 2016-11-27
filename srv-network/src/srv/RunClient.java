package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RunClient implements Runnable {

	private Socket socket;

	public RunClient(Socket client) {

		socket = client;
	}

	@Override
	public void run() {

		boolean go = true;
		int read;
		char[] buffer;
		String strbuf = null;
		buffer = new char[1024];

		while (go) {

			try {

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());

				read = reader.read(buffer);

				if (read == -1) {

					reader.close();
					writer.close();
					socket.close();
					go = false;
					continue;
				}

				strbuf = new String(buffer).substring(0, read);

				// / TODO message parser

			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}
}
