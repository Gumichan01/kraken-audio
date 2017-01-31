package clt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import parser.MessageParser;
import datum.DeviceData;
import datum.GroupData;

public class ClientDevice {

	// Server information
	private static final String SVHOST = "localhost";
	private static final int SVPORT = 8080;

	private URL url;
	private HttpURLConnection connection;

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private String device_name;
	private InetSocketAddress ipaddr;
	private int bport;

	public ClientDevice(String name, String addr, int port, int bport)
			throws MalformedURLException {

		url = new URL("http://localhost:8000");
		this.device_name = name;
		this.ipaddr = new InetSocketAddress(addr, port);
		this.bport = bport;
		// socket = null;
		// reader = null;
		// writer = null;
	}

	private String connectionToServer(String msg) {

		HttpURLConnection connection;

		try {
			connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Length", "" + msg.length());
			OutputStream os = connection.getOutputStream();
			os.write(msg.getBytes());
			connection.setReadTimeout(8000);
			connection.connect();

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

				System.out.println("valid request\n");

				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));

				String line = null;
				StringBuilder stbuild = new StringBuilder("");

				while ((line = reader.readLine()) != null) {
					stbuild.append(line).append(MessageParser.EOL);
				}

				System.out.println(stbuild.toString());
				return stbuild.toString();

			} else {
				System.err.println("response code: "
						+ connection.getResponseCode());
			}

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return null;
	}

	public boolean createGroup(String gname) {

		if (gname == null)
			return false;

		StringBuilder st = new StringBuilder("");

		st.append(MessageParser.CLIENT_CGRP + " ");
		st.append(gname + " " + device_name + " ");
		st.append(ipaddr.getAddress().getHostAddress() + " ");
		st.append(ipaddr.getPort() + " ");
		st.append(bport + MessageParser.EOL);

		String result = connectionToServer(st.toString());
		MessageParser parser = new MessageParser(result);

		if (parser.isWellParsed())
			return parser.getHeader().contains(MessageParser.SRV_GCOK);
		
		return false;
	}

	public boolean joinGroup(String gname) {

		if (gname == null)
			return false;

		StringBuilder st = new StringBuilder("");

		st.append(MessageParser.CLIENT_JGRP + " ");
		st.append(gname + " " + device_name + " ");
		st.append(ipaddr.getAddress().getHostAddress() + " ");
		st.append(ipaddr.getPort() + " ");
		st.append(bport + MessageParser.EOL);

		String result = connectionToServer(st.toString());
		MessageParser parser = new MessageParser(result);

		if (parser.isWellParsed())
			return parser.getHeader().contains(MessageParser.SRV_GJOK);
		
		return false;
	}

	public boolean quitGroup(String gname) {

		if (gname == null)
			return false;

		boolean status = false;
		char[] buffer = new char[1024];

		try {

			socket = new Socket(InetAddress.getByName(SVHOST), SVPORT);

			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			writer.write(MessageParser.CLIENT_QGRP + " " + gname + " "
					+ device_name + MessageParser.EOL);
			writer.flush();

			int read = reader.read(buffer);

			if (read == -1)
				status = false;

			String strbuf = new String(buffer).substring(0, read);
			MessageParser parser = new MessageParser(strbuf);

			if (parser.isWellParsed()) {

				if (parser.getHeader().contains(MessageParser.SRV_QACK))
					status = true;
				else
					status = false;
			} else
				status = false;

			socket.close();

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			writer = null;
			reader = null;
			socket = null;
		}

		return status;
	}

	public List<GroupData> groupList() {

		boolean status = false;
		boolean go = true;
		List<GroupData> group = new ArrayList<>();

		try {

			socket = new Socket(InetAddress.getByName(SVHOST), SVPORT);

			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			writer.write(MessageParser.CLIENT_GRPL + MessageParser.EOL);
			writer.flush();

			while (go) {

				String strbuf = reader.readLine();
				MessageParser parser = new MessageParser(strbuf);

				if (parser.isWellParsed()) {

					if (parser.getHeader().contains(MessageParser.SRV_GDAT)) {

						group.add(new GroupData(parser.getGroup(), parser
								.getNumberOfDevices()));

					} else if (parser.getHeader().contains(
							MessageParser.SRV_EOTR)) {

						status = true;
						go = false;
					} else
						go = false;
				} else
					go = false;
			}

			socket.close();

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			writer = null;
			reader = null;
			socket = null;
		}

		return status ? group : null;
	}

	public List<DeviceData> deviceList(String gname) {

		if (gname == null)
			return null;

		boolean status = false;
		boolean go = true;
		List<DeviceData> devices = new ArrayList<>();

		try {

			socket = new Socket(InetAddress.getByName(SVHOST), SVPORT);

			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			writer.write(MessageParser.CLIENT_DEVL + " " + gname + " "
					+ MessageParser.EOL);
			writer.flush();

			while (go) {

				String strbuf = reader.readLine();
				MessageParser parser = new MessageParser(strbuf);

				if (parser.isWellParsed()) {

					if (parser.getHeader().contains(MessageParser.SRV_DDAT)) {

						devices.add(new DeviceData(parser.getDevice(), parser
								.getIPaddr(), parser.getPort(), parser
								.getBroadcastPort()));

					} else if (parser.getHeader().contains(
							MessageParser.SRV_EOTR)) {

						status = true;
						go = false;
					} else
						go = false;
				} else
					go = false;
			}

			socket.close();

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			writer = null;
			reader = null;
			socket = null;
		}

		return status ? devices : null;
	}

	public static void main(String[] args) throws MalformedURLException {

		//ClientDevice c = new ClientDevice("toto", "192.168.48.2", 45621,2410);
		//System.out.println("create group: " + c.createGroup("toto@GT-01"));
		new ClientDevice("lana", "192.168.48.4", 45645, 2410).joinGroup("toto@GT-01");
		/*
		 * ClientDevice c = new ClientDevice("toto", "192.168.48.2", 45621,
		 * 2410);
		 * 
		 * System.out.println("create group: " + c.createGroup("toto@GT-01"));
		 * new ClientDevice("lana", "192.168.48.4", 45645, 2410)
		 * .joinGroup("toto@GT-01"); new ClientDevice("titi", "192.168.48.5",
		 * 45652, 2410) .joinGroup("toto@GT-01");
		 * 
		 * List<GroupData> listgroup = c.groupList();
		 * 
		 * System.out.println("group list"); System.out.println("----------");
		 * for (GroupData g : listgroup) { System.out.println(g.toString()); }
		 * System.out.println("-----------");
		 * 
		 * List<DeviceData> listdev = c.deviceList("toto@GT-01");
		 * 
		 * System.out.println("device list"); System.out.println("-----------");
		 * for (DeviceData d : listdev) { System.out.println(d.toString()); }
		 * System.out.println("-----------");
		 */

	}

}
