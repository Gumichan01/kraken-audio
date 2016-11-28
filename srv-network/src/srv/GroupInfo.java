package srv;

import java.util.Hashtable;
import java.util.Iterator;

public class GroupInfo {

	private String name;
	private Hashtable<String, DeviceData> devices;

	public GroupInfo(String name) {

		this.name = name;
		devices = new Hashtable<String, DeviceData>();
	}

	public boolean addDevice(String key, DeviceData data) {

		try {
			devices.put(key, data);
		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean removeDevice(String name) {

		try {

			if (devices.containsKey(name)) {

				devices.remove(name);
				return true;
			}

			return false;

		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return false;
		}
	}

	public DeviceData getDevice(String key) {

		try {

			return devices.get(key);

		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return null;
		}
	}

	public Iterator<String> getIterator() {

		return devices.keySet().iterator();
	}

	public int nbDevices() {

		return devices.size();
	}

	public String getName() {

		return name;
	}

	/*
	 * Uncomment this block in order to test the class public static void
	 * main(String[] args) {
	 * 
	 * GroupInfo ginfo = new GroupInfo("toto");
	 * 
	 * ginfo.addDevice("Gumichan01@GT-8189N", new DeviceData("192.168.25.1",
	 * 2408)); ginfo.addDevice("Miku@GT-24N", new DeviceData("192.168.25.2",
	 * 2409)); ginfo.addDevice("trool@shit", new DeviceData("192.168.25.3",
	 * 2407)); ginfo.addDevice("Luka@I-8601t", new DeviceData("192.168.25.4",
	 * 2406));
	 * 
	 * Iterator<String> it = ginfo.getIterator();
	 * System.out.println(ginfo.getName() + " - Devices");
	 * System.out.println(ginfo.nbDevices() + " elements");
	 * 
	 * while (it.hasNext()) {
	 * System.out.println(ginfo.getDevice(it.next()).toString()); }
	 * 
	 * ginfo.removeDevice("trool@shit"); Iterator<String> it2 =
	 * ginfo.getIterator(); System.out .println(ginfo.getName() +
	 * " - Devices (removed one element)"); System.out.println(ginfo.nbDevices()
	 * + " elements");
	 * 
	 * while (it2.hasNext()) {
	 * System.out.println(ginfo.getDevice(it2.next()).toString()); }
	 * 
	 * }
	 */

}
