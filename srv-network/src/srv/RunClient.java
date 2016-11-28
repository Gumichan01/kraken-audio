package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import parser.MessageParser;

public class RunClient implements Runnable {

	public static final String FAIL = "FAIL";
	private static final int SRV_TIMEOUT = 16000;
	private DirectoryServer srv;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	public RunClient(DirectoryServer ds, Socket client) {

		srv = ds;
		socket = client;

		try {

			socket.setSoTimeout(SRV_TIMEOUT);

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
				parser = new MessageParser(strbuf);

				if (parser.isWellParsed()) {

					if (parser.getHeader().equals(MessageParser.CLIENT_CGRP)) {

						srv.newGroup(parser.getGroup());

						if (srv.getGroup(parser.getGroup()).addDevice(
								parser.getDevice(),
								new DeviceData(parser.getIPaddr(), parser
										.getPort()))) {

							writer.write(MessageParser.SRV_GCOK
									+ MessageParser.EOL);
							writer.flush();

						} else {

							writer.write(FAIL + MessageParser.EOL);
							writer.flush();
						}

					} else if (parser.getHeader().equals(
							MessageParser.CLIENT_GRPL)) {

					} else if (parser.getHeader().equals(
							MessageParser.CLIENT_DEVL)) {

					} else if (parser.getHeader().equals(
							MessageParser.CLIENT_JGRP)) {

						if (srv.getGroup(parser.getGroup()).addDevice(
								parser.getDevice(),
								new DeviceData(parser.getIPaddr(), parser
										.getPort()))) {

							writer.write(MessageParser.SRV_GJOK
									+ MessageParser.EOL);
							writer.flush();

						} else {

							writer.write(FAIL + MessageParser.EOL);
							writer.flush();
						}

					} else if (parser.getHeader().equals(
							MessageParser.CLIENT_QGRP)) {

						if (srv.getGroup(parser.getGroup()).removeDevice(
								parser.getDevice())) {

							writer.write(MessageParser.SRV_QACK
									+ MessageParser.EOL);
							writer.flush();

						} else {

							writer.write(FAIL + MessageParser.EOL);
							writer.flush();
						}

					} else if (parser.getHeader().equals(
							MessageParser.CLIENT_EOCO)) {

						closeConnection();
						go = false;
					}

				} else {

					System.out.println("FAIL");
					closeConnection();
					go = false;
				}

			} catch (SocketTimeoutException ste) {

				closeConnection();
				go = false;

			} catch (IOException e) {

				e.printStackTrace();
			} finally {

				parser = null;
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
