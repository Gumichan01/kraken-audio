package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

import parser.MessageParser;

public class RunClient implements Runnable {

	private DirectoryServer srv;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	public RunClient(DirectoryServer ds, Socket client) {

		srv = ds;
		socket = client;

		try {
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		if (reader == null || writer == null)
			return;

		int read;
		boolean go = true;
		MessageParser parser = null;

		char[] buffer;
		String strbuf = null;
		buffer = new char[1024];

		while (go) {

			try {

				read = reader.read(buffer);

				if (read == -1) {

					closeConnection();
					go = false;
					continue;
				}

				strbuf = new String(buffer).substring(0, read);

				System.out.println("read: " + strbuf);

				// / TODO message parser
				parser = new MessageParser(strbuf);

				if (parser.isWellParsed()) {

					System.out.println("SUCCESS");
					System.out.println(parser.getHeader());

					if (parser.getHeader().equals(MessageParser.CLIENT_CGRP)) {

						System.out.println("SUCCESS OK");

						srv.newGroup(parser.getGroup());
						srv.getGroup(parser.getGroup()).addDevice(
								parser.getDevice(),
								new DeviceData(parser.getIPaddr(), parser
										.getPort()));

						/// TODO Remove this block
						Iterator<String> it = srv.getIterator();
						System.out.println("Groups");
						System.out.println(srv.nbGroups() + " groups");

						while (it.hasNext()) {

							GroupInfo g = srv.getGroup(it.next());
							Iterator<String> itg = g.getIterator();
							System.out.println(g.getName() + " - Devices");
							System.out.println(g.nbDevices() + " elements");

							while (itg.hasNext()) {
								System.out.println(g.getDevice(itg.next())
										.toString());
							}

						}
						/// TODO Remove this block end
					}

				} else {
					System.out.println("FAIL");
					closeConnection();
					go = false;
				}

			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

	private void closeConnection() {

		try {

			reader.close();
			writer.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
