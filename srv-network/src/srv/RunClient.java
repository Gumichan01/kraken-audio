package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import datum.DeviceData;
import parser.MessageParser;
import sun.net.www.protocol.http.HttpURLConnection;

public class RunClient implements HttpHandler {

	private static final int BUFSIZE = 1024;
	private static final String REQ_GET = "GET";
	private static final String REQ_POST = "POST";
	private static final String PROP_CONT = "Content-Length";

	private String response;
	private DirectoryServer srv;
	private MessageParser parser = null;

	public RunClient(DirectoryServer s) {

		srv = s;
	}

	public void handle(HttpExchange t) {

		if (t == null)
			return;

		response = null;
		int res = HttpURLConnection.HTTP_OK;
		String req = t.getRequestMethod();
		System.out.println(" - " + req + " - ");

		if (req.equals(REQ_GET) || req.equals(REQ_POST)) {

			int read;
			int toread = BUFSIZE;
			String strbuf = null;
			BufferedReader r = null;
			char[] buffer = new char[BUFSIZE];
			r = new BufferedReader(new InputStreamReader(t.getRequestBody()));

			try {
				// get the headers
				Headers h = t.getRequestHeaders();

				if (h != null) {

					// headers (comment)
					/*Iterator<String> it = h.keySet().iterator();

					while (it.hasNext()) {

						String k = it.next();

						for (String v : h.get(k)) {

							System.out.println(k + ": " + v.toString());
						}
					}*/

					// Use the length of the content
					if (h.containsKey(PROP_CONT)) {

						try {
							toread = Integer.parseInt(h.get(PROP_CONT).get(0));
						} catch (NumberFormatException ne) {
							ne.printStackTrace();
						}
					}
				}

				read = r.read(buffer, 0, toread);

				if (read == -1)
					strbuf = "";
				else {
					strbuf = new String(buffer).substring(0, read);
					// remote address (comment)
					System.out.print(t.getRemoteAddress().toString() + ": " + strbuf);
				}

				parser = new MessageParser(strbuf);

				if (parser.isWellParsed())
					respond();
				else
					response = MessageParser.SRV_BADR + MessageParser.EOL;

				t.sendResponseHeaders(res, response.length());

				if (res == HttpURLConnection.HTTP_OK) {

					OutputStream os = t.getResponseBody();
					os.write(response.getBytes());
					os.flush();
					os.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			res = HttpURLConnection.HTTP_NOT_IMPLEMENTED;
			try {
				t.sendResponseHeaders(res, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		t.close();
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
		else if (parser.getHeader().equals(MessageParser.CLIENT_GRPH))
			graphUpdateResponse();
		
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

	private void graphUpdateResponse() {
		
		/// TODO update the graph
		response = MessageParser.SRV_GPOK + MessageParser.EOL;
	}
	
}
