package datum;

import java.net.InetSocketAddress;

public class DeviceData {

	private String dname;
	private InetSocketAddress ipaddr;
	private int bport;

	public DeviceData(String name, String ipaddress, int port, int bport) {

		ipaddr = new InetSocketAddress(ipaddress, port);
		this.bport = bport;
		dname = name;
	}

	public String getName() {

		return dname;
	}

	public String getAddr() {

		return ipaddr.getAddress().getHostAddress();
	}

	public int getPort() {

		return ipaddr.getPort();
	}

	public int getBroadcastPort() {

		return bport;
	}
	
	public String toString() {

		return dname + " " + getAddr() + " " + getPort() + " " + getBroadcastPort();
	}
}
