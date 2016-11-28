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

	// Création d'un groupe:
	// Rejoindre un groupe spécifique:
	// Avoir la liste des groupes:
	// Avoir la liste des appareils d'un groupe donné:
	// Quitter un groupe:
}
