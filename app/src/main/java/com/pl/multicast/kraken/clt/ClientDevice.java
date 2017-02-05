package com.pl.multicast.kraken.clt;

import android.util.Log;

import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.datum.GroupData;
import com.pl.multicast.kraken.parser.MessageParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class ClientDevice {

	// Server information
	private static final String HTTP_HOST = "http://luxon.hackojo.org";
	private static final String HTTP_METHOD = "POST";
    private static final String HTTP_METADATA = "Content-Length";
    private static final int HTTP_TIMEOUT = 16000;

	private URL url;
	private BufferedReader reader;
	private String device_name;
	private InetSocketAddress ipaddr;
	private int bport;

	public ClientDevice(String name, String addr, int port, int bport)
			throws MalformedURLException {

		url = new URL(HTTP_HOST);
		this.device_name = name;
		this.ipaddr = new InetSocketAddress(addr, port);
		this.bport = bport;
	}

	@SuppressWarnings("finally")
	private String connectionToServer(String msg) {

		HttpURLConnection connection = null;
		StringBuilder stbuild = null;

		try {
			connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod(HTTP_METHOD);
			connection.setDoOutput(true);
			connection.setRequestProperty(HTTP_METADATA, "" + msg.length());
			OutputStream os = connection.getOutputStream();
			os.write(msg.getBytes());
			connection.setReadTimeout(HTTP_TIMEOUT);
			connection.connect();

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

				Log.i("kraken-NETWORK","valid request\n");

				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));

				String line = null;
				stbuild = new StringBuilder("");

				while ((line = reader.readLine()) != null) {
					stbuild.append(line).append(MessageParser.EOL);
				}

                Log.i("kraken-NETWORK", stbuild.toString());

			} else {
                Log.e("kraken-NETWORK", "response code: "
                        + connection.getResponseCode());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			if (connection != null)
				connection.disconnect();

			return stbuild == null ? null : stbuild.toString();
		}
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

		StringBuilder st = new StringBuilder("");

		st.append(MessageParser.CLIENT_QGRP + " ");
		st.append(gname + " " + device_name + MessageParser.EOL);

		String result = connectionToServer(st.toString());
		MessageParser parser = new MessageParser(result);

		if (parser.isWellParsed())
			return parser.getHeader().contains(MessageParser.SRV_QACK);

		return false;
	}

	public List<GroupData> groupList() {

		StringBuilder st = new StringBuilder("");
		List<GroupData> group = new ArrayList<>();

		st.append(MessageParser.CLIENT_GRPL);
		st.append(MessageParser.EOL);

		String result = connectionToServer(st.toString());
		Pattern p = Pattern.compile(MessageParser.EOL);
		String[] tokens = p.split(result);

		if (tokens == null)
			return null;

		for (String s : tokens) {

			MessageParser parser = new MessageParser(s);

			if (parser.isWellParsed()) {

				if (parser.getHeader().contains(MessageParser.SRV_GDAT)) {

					group.add(new GroupData(parser.getGroup(), parser
							.getNumberOfDevices()));

				} else if (parser.getHeader().contains(MessageParser.SRV_EOTR))
					break;
				else
					return null;
			}

		}

		return group;
	}

	public List<DeviceData> deviceList(String gname) {

		if (gname == null)
			return null;

		StringBuilder st = new StringBuilder("");
		List<DeviceData> devices = new ArrayList<>();

		st.append(MessageParser.CLIENT_DEVL + " ");
		st.append(gname + " ");
		st.append(MessageParser.EOL);

		String result = connectionToServer(st.toString());
		Pattern p = Pattern.compile(MessageParser.EOL);
		String[] tokens = p.split(result);

		if (tokens == null)
			return null;

		for (String s : tokens) {

			MessageParser parser = new MessageParser(s);

			if (parser.isWellParsed()) {

				if (parser.getHeader().contains(MessageParser.SRV_DDAT)) {

					devices.add(new DeviceData(parser.getDevice(), parser
							.getIPaddr(), parser.getPort(), parser
							.getBroadcastPort()));

				} else if (parser.getHeader().contains(MessageParser.SRV_EOTR))
					break;
				else
					return null;
			}

		}

		return devices;
	}
}
