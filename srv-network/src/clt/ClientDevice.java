package clt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import parser.MessageParser;

import datum.DeviceData;
import datum.GroupData;

public class ClientDevice {

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private String device_name;
	private InetSocketAddress ipaddr;

	public ClientDevice(String name, String addr, int port) {
		this.device_name = name;
		this.ipaddr = new InetSocketAddress(addr, port);
		try {
			socket = new Socket(InetAddress.getByName("localhost"), 8080);
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));
		} catch (UnknownHostException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public boolean createGroup(String gname) {
		char[] buffer = new char[1024];

		if (gname == null)
			return false;

		writer.write(MessageParser.CLIENT_CGRP + " " + gname + " "
				+ device_name + " " + ipaddr.getAddress().getHostAddress()
				+ " " + ipaddr.getPort() + MessageParser.EOL);
		writer.flush();

		try {
			int read = reader.read(buffer);

			if (read == -1) {
				return false;
			}

			String strbuf = new String(buffer).substring(0, read);
			MessageParser parser = new MessageParser(strbuf);

			if (parser.isWellParsed()) {
				if (parser.getHeader().contains(MessageParser.SRV_GCOK)) {
					return true;
				} else
					return false;
			} else
				return false;

		} catch (IOException e) {

			e.printStackTrace();
		}
		return true;
	}

	public void close() {
		writer.write(MessageParser.CLIENT_EOCO);
		try {
			socket.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		reader = null;
		writer = null;
	}

	public boolean joinGroup(String gname) {
		char[] buffer = new char[1024];

		if (gname == null)
			return false;

		writer.write(MessageParser.CLIENT_JGRP + " " + gname + " "
				+ device_name + " " + ipaddr.getAddress().getHostAddress()
				+ " " + ipaddr.getPort() + MessageParser.EOL);
		writer.flush();

		try {
			int read = reader.read(buffer);

			if (read == -1) {
				return false;
			}

			String strbuf = new String(buffer).substring(0, read);
			MessageParser parser = new MessageParser(strbuf);

			if (parser.isWellParsed()) {
				if (parser.getHeader().contains(MessageParser.SRV_GJOK)) {
					return true;
				} else
					return false;
			} else
				return false;

		} catch (IOException e) {

			e.printStackTrace();
		}
		return true;
	}

	public boolean quitGroup(String gname) {
		char[] buffer = new char[1024];

		if (gname == null)
			return false;

		writer.write(MessageParser.CLIENT_QGRP + " " + gname + " "
				+ device_name + MessageParser.EOL);
		writer.flush();

		try {
			int read = reader.read(buffer);

			if (read == -1) {
				return false;
			}

			String strbuf = new String(buffer).substring(0, read);
			MessageParser parser = new MessageParser(strbuf);

			if (parser.isWellParsed()) {
				if (parser.getHeader().contains(MessageParser.SRV_QACK)) {
					return true;
				} else
					return false;
			} else
				return false;

		} catch (IOException e) {

			e.printStackTrace();
		}

		return true;
	}

	public List<GroupData> groupList(String gname) {

		List<GroupData> group = new ArrayList<>();

		if (gname == null)
			return null;

		writer.write(MessageParser.CLIENT_GRPL + MessageParser.EOL);
		writer.flush();

		while (true) {

			try {

				String strbuf = reader.readLine();
				MessageParser parser = new MessageParser(strbuf);

				if (parser.isWellParsed()) {

					if (parser.getHeader().contains(MessageParser.SRV_GDAT)) {
						GroupData newgroup = new GroupData(parser.getGroup(),
								parser.getNumberOfDevices());
						group.add(newgroup);
					} else if (parser.getHeader().contains(
							MessageParser.SRV_EOTR))
						return group;
					else
						return null;
				} else
					return null;

			} catch (IOException e) {

				e.printStackTrace();
				return null;
			}
		}
	}

	public List<DeviceData> deviceList(String gname) {

		List<DeviceData> devices = new ArrayList<>();

		if (gname == null)
			return null;

		writer.write(MessageParser.CLIENT_DEVL + " " + gname + " "
				+ MessageParser.EOL);
		writer.flush();

		while (true) {
			try {

				String strbuf = reader.readLine();
				MessageParser parser = new MessageParser(strbuf);

				if (parser.isWellParsed()) {

					if (parser.getHeader().contains(MessageParser.SRV_DDAT)) {

						DeviceData newdevice = new DeviceData(
								parser.getDevice(), parser.getIPaddr(),
								parser.getPort());
						devices.add(newdevice);
					} else if (parser.getHeader().contains(
							MessageParser.SRV_EOTR))
						return devices;
					else
						return null;
				} else {
					System.out.println("FUCK");
					return null;
				}

			} catch (IOException e) {

				e.printStackTrace();
				return null;
			}
		}
	}

	public static void main(String[] args) {

		ClientDevice c = new ClientDevice("toto", "192.168.48.2", 45621);

		c.createGroup("toto@GT-01");
		new ClientDevice("lana", "192.168.48.4", 45645).joinGroup("toto@GT-01");
		new ClientDevice("titi", "192.168.48.5", 45652).joinGroup("toto@GT-01");

		List<GroupData> listgroup = c.groupList("toto@GT-01");

		System.out.println("group list");
		System.out.println("----------");
		for (GroupData g : listgroup) {
			System.out.println(g.getName() + " " + g.getNumberOfDevices());
		}
		System.out.println("-----------");

		List<DeviceData> listdev = c.deviceList("toto@GT-01");

		System.out.println("device list");
		System.out.println("-----------");
		for (DeviceData d : listdev) {
			System.out.println(d.getName() + " " + d.getAddr() + "/"
					+ d.getPort());
		}
		System.out.println("-----------");
	}

}
