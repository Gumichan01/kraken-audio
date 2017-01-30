package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import datum.DeviceData;
import parser.MessageParser;
import sun.net.www.protocol.http.HttpURLConnection;

public class RunClient implements HttpHandler {

	private static final String REQ_GET = "GET";
	private static final String REQ_POST = "POST";

	// / remove
	private DirectoryServer srv;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	// / end remove

	String response;

	boolean closed = false;
	MessageParser parser = null;

	public RunClient() {

	}

	public void handle(HttpExchange t) {

		if (t == null)
			return;

		response = null;

		if (t.getRequestMethod().equals(REQ_GET)
				|| t.getRequestMethod().equals(REQ_POST)) {

			int read;
			String strbuf = null;
			BufferedReader r = null;
			char[] buffer = new char[1024];
			r = new BufferedReader(new InputStreamReader(t.getRequestBody()));

			try {
				read = r.read(buffer);

				if (read == -1)
					strbuf = "";
				else {
					new String(buffer).substring(0, read);
					System.out.println(strbuf);
				}

				parser = new MessageParser(strbuf);
				
				if (parser.isWellParsed()) {

					respond();

				} else {

					response = new StringBuilder(MessageParser.SRV_BADR)
							.append(MessageParser.EOL).toString();

					t.sendResponseHeaders(HttpURLConnection.HTTP_OK,
							response.length());

					OutputStream os = t.getResponseBody();
					os.write(response.getBytes());
					os.flush();
					os.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			// respond();
		}

	}

	/**
	 * 
	 * @deprecated
	 * 
	 * */
	public void run() {

		if (reader == null || writer == null)
			return;

		int read;
		char[] buffer;
		String strbuf = null;
		buffer = new char[1024];

		try {

			System.out.print("from " + socket.getInetAddress().getHostAddress()
					+ ":" + socket.getPort() + ": ");
			read = reader.read(buffer);

			if (read == -1)
				throw new IOException("data reading failed");

			strbuf = new String(buffer).substring(0, read);
			parser = new MessageParser(strbuf);

			System.out.println(strbuf);

			if (parser.isWellParsed()) {

				respond();

			} else {

				writer.write(MessageParser.SRV_BADR + MessageParser.EOL);
				writer.flush();
			}

		} catch (SocketTimeoutException | SocketException ste) {

			ste.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			parser = null;
			closeConnection();
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

		} else if (parser.getHeader().equals(MessageParser.CLIENT_QGRP))
			quitGroupResponse();
	}

	private void groupCreationResponse() {

		if (srv.getGroup(parser.getGroup()) != null) {

			writer.write(MessageParser.SRV_FAIL + MessageParser.EOL);
			writer.flush();
			return;
		}

		srv.newGroup(parser.getGroup());

		if (srv.getGroup(parser.getGroup()).addDevice(
				parser.getDevice(),
				new DeviceData(parser.getDevice(), parser.getIPaddr(), parser
						.getPort(), parser.getBroadcastPort()))) {

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

				writer.write(MessageParser.SRV_DDAT + " "
						+ g.getDevice(dname).toString() + MessageParser.EOL);
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
						.getPort(), parser.getBroadcastPort()))) {

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
