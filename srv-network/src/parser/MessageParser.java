package parser;

import java.util.regex.Pattern;

public class MessageParser {

	// / Client request
	// Group creation
	public static final String CLIENT_CGRP = "CGRP";
	// Group list
	public static final String CLIENT_GRPL = "GRPL";
	// Device lsit
	public static final String CLIENT_DEVL = "DEVL";
	// Join group
	public static final String CLIENT_JGRP = "JGRP";
	// Quit group
	public static final String CLIENT_QGRP = "QGRP";
	// EOCO
	public static final String CLIENT_EOCO = "EOCO";

	// / Server answer
	// / TODO final strings for server message header

	// Additional information
	private static final int HEADER_SIZE = 4;
	private static final String SPACE = "\\s";

	boolean well_parsed;
	String message;
	String header;
	String group_name;
	String device_name;
	String ipaddr;
	int port;

	public MessageParser(String s) {

		message = s;
		parse();
	}

	private void parse() {

		if (message == null)
			return;

		// Look at the value of the header string
		header = message.substring(0, HEADER_SIZE);

		if (header.equals(CLIENT_CGRP))
			parseCGRP();
		else
			well_parsed = false;
	}

	private void parseCGRP() {

		int nbwords = 5;
		Pattern p = Pattern.compile(SPACE);
		String[] tokens = p.split(message);

		if (tokens.length != nbwords)
			well_parsed = false;
		else {
			group_name = tokens[1];
			device_name = tokens[2];
			ipaddr = tokens[3];
			port = Integer.parseInt(tokens[4]);
			well_parsed = true;
		}

	}

	public boolean isWellParsed() {

		return well_parsed;
	}

	public String getHeader() {

		return header;
	}

	public String getGroup() {

		return group_name;
	}

	public String getDevice() {

		return device_name;
	}

	public String getIPaddr() {

		return ipaddr;
	}

	public int getPort() {

		return port;
	}

}
