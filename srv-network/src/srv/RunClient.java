package srv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
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
		String req = t.getRequestMethod();
		System.out.println(" - " + req + " - ");

		if (req.equals(REQ_GET))
			handleGet(t);

		else if (req.equals(REQ_POST))
			handlePost(t);

		else {
			try {
				t.sendResponseHeaders(HttpURLConnection.HTTP_NOT_IMPLEMENTED, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		t.close();
	}

	private void handleGet(HttpExchange t) {

		try {

			response = "<html><title>It works</title>"
					+ "<body><h1>It works</h1>This server works!</body></html>";
			t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.flush();
			os.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handlePost(HttpExchange t) {

		int read;
		int toread = BUFSIZE;
		String strbuf = null;
		BufferedReader r = null;
		char[] buffer = new char[BUFSIZE];
		int res = HttpURLConnection.HTTP_OK;

		r = new BufferedReader(new InputStreamReader(t.getRequestBody()));

		try {
			// get the headers
			Headers h = t.getRequestHeaders();

			if (h != null) {
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
				System.out.print(t.getRemoteAddress().toString() + ": "
						+ strbuf);
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
		else if (parser.getHeader().equals(MessageParser.CLIENT_GGPH))
			getGraphResponse();
		else if (parser.getHeader().equals(MessageParser.CLIENT_IAMH))
			updateDeviceResponse();
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

			srv.graph.addEdge(parser.getDevice()); // / new edge
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

			if (b)
				srv.graph.addEdge(parser.getDevice()); // / new edge

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

			srv.graph.rmEdge(parser.getDevice()); // / remove edge
			response = MessageParser.SRV_QACK + MessageParser.EOL;
		}
	}

	private void graphUpdateResponse() {

		boolean res = false;
		if (parser.getOp().equals(MessageParser.ARROW))
			res = srv.graph.link(parser.getSource(), parser.getDest());

		else if (parser.getOp().equals(MessageParser.CROSS))
			res = srv.graph.unlink(parser.getSource(), parser.getDest());

		response = (res ? MessageParser.SRV_GPOK : MessageParser.SRV_FAIL)
				+ MessageParser.EOL;
	}

	private void getGraphResponse() {

		ArrayList<String> paths = srv.graph.getPaths(parser.getDevice());
		StringBuilder sb = new StringBuilder("");

		if (paths == null)
			response = MessageParser.SRV_FAIL + MessageParser.EOL;
		else {

			for (String s : paths) {
				sb.append(MessageParser.SRV_PATH).append(" ");
				sb.append(s).append(MessageParser.EOL);
			}

			sb.append(MessageParser.SRV_EOTR).append(MessageParser.EOL);
			response = sb.toString();
		}

	}

	private void updateDeviceResponse() {

		// / TODO update the state of the device
		response = MessageParser.SRV_UACK + MessageParser.EOL;
	}
}
