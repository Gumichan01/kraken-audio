package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import datum.DeviceData;

import parser.MessageParser;

public class RunClient implements Runnable {

	private static final int SRV_TIMEOUT = 0;

	private DirectoryServer srv;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	boolean closed = false;
	MessageParser parser = null;

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

					respond();
					go = closed ? false : true; // Is the connection closed?

				} else {

					writer.write(MessageParser.SRV_BADR + MessageParser.EOL);
					writer.flush();
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

	private void respond() {

		if (parser.getHeader().equals(MessageParser.CLIENT_CGRP)) {

			groupCreationResponse();

		} else if (parser.getHeader().equals(MessageParser.CLIENT_GRPL)) {

			groupListResponse();

		} else if (parser.getHeader().equals(MessageParser.CLIENT_DEVL)) {

			deviceListResponse();

		} else if (parser.getHeader().equals(MessageParser.CLIENT_JGRP)) {

			joinGroupResponse();

		} else if (parser.getHeader().equals(MessageParser.CLIENT_QGRP)) {

			quitGroupResponse();

		} else if (parser.getHeader().equals(MessageParser.CLIENT_EOCO)) {

			closeConnection();
		}
	}

	private void groupCreationResponse() {

		srv.newGroup(parser.getGroup());

		if (srv.getGroup(parser.getGroup()).addDevice(
				parser.getDevice(),
				new DeviceData(parser.getDevice(), parser.getIPaddr(), parser
						.getPort()))) {

			writer.write(MessageParser.SRV_GCOK + MessageParser.EOL);
			writer.flush();

		} else {

			writer.write(MessageParser.SRV_FAIL + MessageParser.EOL);
			writer.flush();
		}
	}

	private void groupListResponse() {

		Iterator<String> it = srv.getIterator();

		while (it.hasNext()) {

			writer.write(MessageParser.SRV_GDAT + " "
					+ srv.getGroup(it.next()).toString() + MessageParser.EOL);
			writer.flush();
		}

		writer.write(MessageParser.SRV_EOTR + MessageParser.EOL);
		writer.flush();
	}

	private void deviceListResponse() {

		GroupInfo g = srv.getGroup(parser.getGroup());

		if (g == null) {

			writer.write(MessageParser.SRV_FAIL + MessageParser.EOL);
			writer.flush();

		} else {

			Iterator<String> it = g.getIterator();

			while (it.hasNext()) {

				String dname = it.next();

				writer.write(MessageParser.SRV_DDAT + " " + dname + " "
						+ g.getDevice(dname).toString() + MessageParser.EOL);
				writer.flush();
			}

			writer.write(MessageParser.SRV_EOTR + MessageParser.EOL);
			writer.flush();
		}
	}

	private void joinGroupResponse() {

		GroupInfo g = srv.getGroup(parser.getGroup());

		if (g == null) {

			writer.write(MessageParser.SRV_FAIL + MessageParser.EOL);
			writer.flush();

		} else if (g.addDevice(
				parser.getDevice(),
				new DeviceData(parser.getDevice(), parser.getIPaddr(), parser
						.getPort()))) {

			writer.write(MessageParser.SRV_GJOK + MessageParser.EOL);
			writer.flush();

		} else {

			writer.write(MessageParser.SRV_FAIL + MessageParser.EOL);
			writer.flush();
		}
	}

	private void quitGroupResponse() {

		GroupInfo g = srv.getGroup(parser.getGroup());

		if (g == null) {

			writer.write(MessageParser.SRV_FAIL + MessageParser.EOL);
			writer.flush();

		} else if (g.removeDevice(parser.getDevice())) {

			if (g.nbDevices() == 0)
				srv.destroyGroup(g.getName());

			writer.write(MessageParser.SRV_QACK + MessageParser.EOL);
			writer.flush();

		} else {

			writer.write(MessageParser.SRV_FAIL + MessageParser.EOL);
			writer.flush();
		}
	}

	private void closeConnection() {

		try {

			reader.close();
			writer.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {

			closed = true;
		}

	}
}
