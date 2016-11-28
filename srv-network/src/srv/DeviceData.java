package srv;

import java.net.InetSocketAddress;

public class DeviceData {

	private InetSocketAddress ipaddr;

	public DeviceData(String ipaddress, int port) {

		ipaddr = new InetSocketAddress(ipaddress, port);
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
