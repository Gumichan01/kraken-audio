package srv;

public class DeviceData {

	private String ipaddr;
	private int port;

	public DeviceData(String ipaddr, int port) {
		this.ipaddr = ipaddr;
		this.port = port;
	}

	public String getAddr() {

		return ipaddr;
	}

	public int getPort() {

		return port;
	}

	public String toString() {

		return "" + ipaddr + " " + port;
	}

}
