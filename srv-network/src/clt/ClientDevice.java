package clt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import parser.MessageParser;

import datum.DeviceData;
import datum.GroupData;

public class ClientDevice {

	// Server information
	private static final String SVHOST = "localhost";
	private static final int SVPORT = 8080;

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private String device_name;
	private InetSocketAddress ipaddr;
	private int bport;

	public ClientDevice(String name, String addr, int port, int bport) {

		this.device_name = name;
		this.ipaddr = new InetSocketAddress(addr, port);
		this.bport = bport;
		socket = null;
		reader = null;
		writer = null;
	}

	public boolean createGroup(String gname) {

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

			writer.write(MessageParser.CLIENT_CGRP + " " + gname + " "
					+ device_name + " " + ipaddr.getAddress().getHostAddress()
					+ " " + ipaddr.getPort() + " " + bport + MessageParser.EOL);
			writer.flush();
			
			int read = reader.read(buffer);

			if (read == -1)
				status = false;
			else {
				
				String strbuf = new String(buffer).substring(0, read);
				MessageParser parser = new MessageParser(strbuf);

				if (parser.isWellParsed()) {

					if (parser.getHeader().contains(MessageParser.SRV_GCOK))
						status = true;
					else
						status = false;
				} else
					status = false;
			}

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


	public boolean joinGroup(String gname) {

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
			
			writer.write(MessageParser.CLIENT_JGRP + " " + gname + " "
					+ device_name + " " + ipaddr.getAddress().getHostAddress()
					+ " " + ipaddr.getPort() + " " + bport + MessageParser.EOL);
			writer.flush();
			
			int read = reader.read(buffer);

			if (read == -1)
				status = false;

			String strbuf = new String(buffer).substring(0, read);
			MessageParser parser = new MessageParser(strbuf);

			if (parser.isWellParsed()) {

				if (parser.getHeader().contains(MessageParser.SRV_GJOK))
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

			while(go){

				String strbuf = reader.readLine();
				MessageParser parser = new MessageParser(strbuf);
				
				if (parser.isWellParsed()) {

					if (parser.getHeader().contains(MessageParser.SRV_GDAT)) {

						group.add(new GroupData(parser.getGroup(), parser
								.getNumberOfDevices()));

					} else if (parser.getHeader().contains(
							MessageParser.SRV_EOTR)){
						
						status = true;
						go = false;
					}
					else
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

		return status ? group: null;
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

			while(go){
				
				String strbuf = reader.readLine();
				MessageParser parser = new MessageParser(strbuf);

				if (parser.isWellParsed()) {

					if (parser.getHeader().contains(MessageParser.SRV_DDAT)) {

						devices.add(new DeviceData(parser.getDevice(), parser
								.getIPaddr(), parser.getPort(), parser
								.getBroadcastPort()));

					} else if (parser.getHeader().contains(
							MessageParser.SRV_EOTR)){
					
						status = true;
						go = false;
					}
					else
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
		
		return status ? devices: null;
	}


	public static void main(String[] args) {

		ClientDevice c = new ClientDevice("toto", "192.168.48.2", 45621, 2410);

		System.out.println("create group: " + c.createGroup("toto@GT-01"));
		new ClientDevice("lana", "192.168.48.4", 45645, 2410)
				.joinGroup("toto@GT-01");
		new ClientDevice("titi", "192.168.48.5", 45652, 2410)
				.joinGroup("toto@GT-01");
		
		List<GroupData> listgroup = c.groupList();

		System.out.println("group list");
		System.out.println("----------");
		for (GroupData g : listgroup) {
			System.out.println(g.toString());
		}
		System.out.println("-----------");

		List<DeviceData> listdev = c.deviceList("toto@GT-01");

		System.out.println("device list");
		System.out.println("-----------");
		for (DeviceData d : listdev) {
			System.out.println(d.toString());
		}
		System.out.println("-----------");

	}

}
