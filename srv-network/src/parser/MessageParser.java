package parser;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

public class MessageParser {

	public static final String EOL = "\r\n";

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
	// Group creation OK
	public static final String SRV_GCOK = "GCOK";
	// Join group OK
	public static final String SRV_GJOK = "GJOK";
	// Quit acknowlegdment
	public static final String SRV_QACK = "QACK";
	// / TODO final strings for server message header

	// Additional information
	private static final int HEADER_SIZE = 4;
	private static final String SPACE = "\\s";

	boolean well_parsed;
	String message;
	String header;
	String group_name;
	String device_name;
	InetSocketAddress ipaddr;

	public MessageParser(String s) {

		message = s;
		parse();
	}

	private void parse() {

		if (message == null || message.isEmpty()
				|| message.length() < HEADER_SIZE)
			return;

		// Look at the value of the header string
		header = message.substring(0, HEADER_SIZE);

		if (header.equals(CLIENT_CGRP))
			parseCGRP();

		else if (header.equals(CLIENT_GRPL))
			parseGRPL();

		else if (header.equals(CLIENT_DEVL))
			parseDEVL();

		else if (header.equals(CLIENT_JGRP))
			parseJGRP();

		else if (header.equals(CLIENT_QGRP))
			parseQGRP();

		else if (header.equals(CLIENT_EOCO))
			parseEOCO();

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
			ipaddr = new InetSocketAddress(tokens[3],
					Integer.parseInt(tokens[4]));
			well_parsed = true;
		}

	}

	private void parseGRPL() {

		well_parsed = true;
	}

	private void parseDEVL() {

		int nbwords = 2;
		Pattern p = Pattern.compile(SPACE);
		String[] tokens = p.split(message);

		if (tokens.length != nbwords)
			well_parsed = false;
		else {
			group_name = tokens[1];
			well_parsed = true;
		}
	}

	private void parseJGRP() {

		int nbwords = 5;
		Pattern p = Pattern.compile(SPACE);
		String[] tokens = p.split(message);

		if (tokens.length != nbwords)
			well_parsed = false;
		else {
			group_name = tokens[1];
			device_name = tokens[2];
			ipaddr = new InetSocketAddress(tokens[3],
					Integer.parseInt(tokens[4]));
			well_parsed = true;
		}

	}

	private void parseQGRP() {

		int nbwords = 3;
		Pattern p = Pattern.compile(SPACE);
		String[] tokens = p.split(message);

		if (tokens.length != nbwords)
			well_parsed = false;
		else {
			group_name = tokens[1];
			device_name = tokens[2];
			well_parsed = true;
		}
	}

	private void parseEOCO() {

		well_parsed = true;
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

		return ipaddr.getAddress().getHostAddress();
	}

	public int getPort() {

		return ipaddr.getPort();
	}

}
