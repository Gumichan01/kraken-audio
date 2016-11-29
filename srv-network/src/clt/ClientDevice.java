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
import java.util.List;

import parser.MessageParser;

import datum.DeviceData;
import datum.GroupData;


public class ClientDevice{
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private String device_name;
	private InetSocketAddress ipaddr;
		
	public ClientDevice(String name, String addr, int port) {
		this.device_name = name;
		this.ipaddr = new InetSocketAddress(addr, port);
		try {
			socket = new Socket(InetAddress.getByName("localhost"), 2048);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean createGroup(String gname){
		char[] buffer = new char[1024];
		
		if(gname == null)
			return false;
		
		writer.write(MessageParser.CLIENT_CGRP + " " + gname + " " + device_name + " " + ipaddr.getAddress().getHostAddress() + " " + ipaddr.getPort() + MessageParser.EOL);
		writer.flush();
		
		try {
			int read = reader.read(buffer);
			
			if(read == -1){
				return false;
			}
			
			String strbuf = new String(buffer).substring(0, read);
			MessageParser parser = new MessageParser(strbuf);
			
			if(parser.isWellParsed()){
				if(parser.getHeader().contains(MessageParser.SRV_GCOK)){
					System.out.println("SUCCESS");
					return true;
				}
				else
					return false;
			}
			else
				return false;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public void close(){
		writer.write(MessageParser.CLIENT_EOCO);
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader = null;
		writer = null;
	}
	
	public boolean joinGroup(String gname){
		return false;
	}
	
	public boolean quitGroup(String gname){
		char[] buffer = new char[1024];
		
		if(gname == null)
			return false;
		
		writer.write(MessageParser.CLIENT_QGRP + " " + gname + " " + device_name + MessageParser.EOL);
		writer.flush();
		
		try {
			int read = reader.read(buffer);
			
			if(read == -1){
				return false;
			}
			
			String strbuf = new String(buffer).substring(0, read);
			MessageParser parser = new MessageParser(strbuf);
			
			if(parser.isWellParsed()){
				if(parser.getHeader().contains(MessageParser.SRV_QACK)){
					System.out.println("SUCCESS");
					return true;
				}
				else
					return false;
			}
			else
				return false;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public List<GroupData> groupList(String gname){
		return null;
	}
	
	public List<DeviceData> deviceList(String gname){
		return null;
	}
	
	// Rejoindre un groupe spécifique:
	// Avoir la liste des groupes:
	// Avoir la liste des appareils d'un groupe donné:
	// Quitter un groupe:
}
