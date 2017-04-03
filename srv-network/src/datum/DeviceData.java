package datum;

import java.net.InetSocketAddress;

public class DeviceData {

	private String dname;
	private InetSocketAddress ipaddr;
	private int bport;
	private long timestamp;

	public DeviceData(String name, String ipaddress, int port, int bport) {

		ipaddr = new InetSocketAddress(ipaddress, port);
		this.bport = bport;
		dname = name;
		// Current time
		timestamp = System.currentTimeMillis();
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
	
	public long getTimeStamp() {
		
		return timestamp;
	}
	
	public void updateTimeStamp() {
		
		System.out.println("timestamp before update: " + timestamp);
		timestamp = System.currentTimeMillis();
		System.out.println("timestamp after update: " + timestamp);
	}
	
	public String toString() {

		return dname + " " + getAddr() + " " + getPort() + " " + getBroadcastPort();
	}
}
