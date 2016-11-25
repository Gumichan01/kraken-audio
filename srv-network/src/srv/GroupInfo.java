package srv;

import java.util.Hashtable;

public class GroupInfo {
	
	private String name;
	private Hashtable<String, DeviceData> devices;
	
	public GroupInfo(String name){
		this.name = name;
		devices = new Hashtable<>();
	}
	
	public boolean addDevice(String key, DeviceData data){
		devices.put(key, data);
		return true;
	}
}
