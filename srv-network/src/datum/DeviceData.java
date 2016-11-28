package datum;

import java.net.InetSocketAddress;

public class DeviceData {

	private String dname;
	private InetSocketAddress ipaddr;

	public DeviceData(String name, String ipaddress, int port) {

		ipaddr = new InetSocketAddress(ipaddress, port);
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

	public String toString() {

		return getAddr() + " " + getPort();
	}
}
