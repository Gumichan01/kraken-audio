package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import datum.DeviceData;
import parser.MessageParser;
import sun.net.www.protocol.http.HttpURLConnection;

public class RunClient implements HttpHandler {

	private static final String REQ_GET = "GET";
	private static final String REQ_POST = "POST";

	private String response;
	private DirectoryServer srv;
	private MessageParser parser = null;

	public RunClient() {

	}

	public void handle(HttpExchange t) {

		if (t == null)
			return;

		response = null;

		if (t.getRequestMethod().equals(REQ_GET)
				|| t.getRequestMethod().equals(REQ_POST)) {

			int read, res;
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
					res = HttpURLConnection.HTTP_OK;

				} else {

					response = MessageParser.SRV_BADR + MessageParser.EOL;
					res = HttpURLConnection.HTTP_BAD_REQUEST;
				}

				t.sendResponseHeaders(res, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.flush();
				os.close();
				
			} catch (IOException e) {
				e.printStackTrace();
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

		} else if (parser.getHeader().equals(MessageParser.CLIENT_QGRP))
			quitGroupResponse();
	}

	private void groupCreationResponse() {

		if (srv.getGroup(parser.getGroup()) != null) {

			response = MessageParser.SRV_FAIL + MessageParser.EOL;
			return;
		}

		srv.newGroup(parser.getGroup());

		if (srv.getGroup(parser.getGroup()).addDevice(
				parser.getDevice(),
				new DeviceData(parser.getDevice(), parser.getIPaddr(), parser
						.getPort(), parser.getBroadcastPort()))) {

			response = MessageParser.SRV_GCOK + MessageParser.EOL;

		} else {

			response = MessageParser.SRV_FAIL + MessageParser.EOL;
		}
	}

	private void groupListResponse() {

		Iterator<String> it = srv.getIterator();
		StringBuilder sb = new StringBuilder("");

		while (it.hasNext()) {

			sb.append(MessageParser.SRV_GDAT);
			sb.append(" ");
			sb.append(srv.getGroup(it.next()).toString());
			sb.append(MessageParser.EOL);
		}

		sb.append(MessageParser.SRV_EOTR).append(MessageParser.EOL);
		response = sb.toString();
	}

	private void deviceListResponse() {

		GroupInfo g = srv.getGroup(parser.getGroup());
		StringBuilder sb = new StringBuilder("");

		if (g == null)
			response = MessageParser.SRV_FAIL + MessageParser.EOL;
		else {

			Iterator<String> it = g.getIterator();

			while (it.hasNext()) {

				String dname = it.next();
				sb.append(MessageParser.SRV_DDAT);
				sb.append(" ");
				sb.append(g.getDevice(dname).toString());
				sb.append(MessageParser.EOL);
			}

			sb.append(MessageParser.SRV_EOTR).append(MessageParser.EOL);
			response = sb.toString();
		}
	}

	private void joinGroupResponse() {

		GroupInfo g = srv.getGroup(parser.getGroup());

		if (g == null)
			response = MessageParser.SRV_FAIL + MessageParser.EOL;
		else {

			DeviceData d = new DeviceData(parser.getDevice(),
					parser.getIPaddr(), parser.getPort(),
					parser.getBroadcastPort());

			boolean b = g.addDevice(parser.getDevice(), d);

			response = (b ? MessageParser.SRV_GJOK : MessageParser.SRV_FAIL)
					+ MessageParser.EOL;
		}
	}

	private void quitGroupResponse() {

		GroupInfo g = srv.getGroup(parser.getGroup());

		if (g == null || !g.removeDevice(parser.getDevice()))
			response = MessageParser.SRV_FAIL + MessageParser.EOL;
		else {

			if (g.nbDevices() == 0)
				srv.destroyGroup(g.getName());

			response = MessageParser.SRV_QACK + MessageParser.EOL;
		}
	}
}
