package parser;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

public class MessageParser {

	public static final String EOL = "\r\n";
	public static final String SRV_FAIL = "FAIL";
	public static final String SRV_BADR = "BADR";

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
	// End of transmission
	public static final String SRV_EOTR = "EOTR";
	// - List of groups- Group data
	public static final String SRV_GDAT = "GDAT";
	// - List of devices- device data
	public static final String SRV_DDAT = "DDAT";

	// Additional information
	private static final int HEADER_SIZE = 4;
	private static final String SPACE = "\\s";

	boolean well_parsed;
	String message;
	String header;
	String group_name;
	String device_name;
	int devices_number;
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

		// Client message
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

		// Server message
		else if (header.equals(SRV_GCOK) || header.equals(SRV_GJOK)
				|| header.equals(SRV_QACK) || header.equals(SRV_EOTR)
				|| header.equals(SRV_BADR) || header.equals(SRV_FAIL))

			parseOK();

		else if (header.equals(SRV_GDAT))
			parseGDAT();
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

	private void parseOK() {

		well_parsed = true;
	}

	private void parseEOCO() {

		parseOK();
	}

	private void parseGDAT() {

		int nbwords = 3;
		Pattern p = Pattern.compile(SPACE);
		String[] tokens = p.split(message);

		if (tokens.length != nbwords)
			well_parsed = false;

		else {

			group_name = tokens[1];
			devices_number = Integer.parseInt(tokens[2]);
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

		return ipaddr.getAddress().getHostAddress();
	}

	public int getPort() {

		return ipaddr.getPort();
	}

}
