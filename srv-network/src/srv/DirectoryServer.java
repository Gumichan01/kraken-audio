package srv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;

public class DirectoryServer {

	private static final int BUFFER_SIZE = 1024;

	ServerSocket srvsock;
	char [] buffer;
	private Hashtable<String, GroupInfo> groups;

	public DirectoryServer() {
		groups = new Hashtable<String, GroupInfo>();
		srvsock = null;
	}


	public void launch(){

		try {

			srvsock = new ServerSocket(0);
			buffer = new char [BUFFER_SIZE];

			System.out.println("Server @" +
							  srvsock.getInetAddress().getHostAddress() +
							  " " + srvsock.getLocalPort());

			while(true){

				Socket socket = srvsock.accept();

				if(socket == null){
					srvsock.close();
					break;
				}

				// Create a thread that handle the connection
				new Thread(new RunClient(socket)).start();
			}

		} catch (IOException e) {

			e.printStackTrace();
			System.exit(-1);
		}
	}


	public boolean newGroup(String name) {

		try {

			groups.put(name, new GroupInfo(name));
			return true;

		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return false;
		}

	}

	public boolean destroyGroup(String name) {

		try {

			if (groups.containsKey(name)) {

				groups.remove(name);
				return true;
			}

			return false;

		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return false;
		}

	}

	public GroupInfo getGroup(String key) {

		try {

			return groups.get(key);

		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return null;
		}
	}

	public Iterator<String> getIterator() {

		return groups.keySet().iterator();
	}

	public int nbGroups() {

		return groups.size();
	}

	/*
	 * Uncomment this block in order to test the class
	public static void main(String[] args) {

		DirectoryServer srv = new DirectoryServer();

		srv.newGroup("toto");
		srv.newGroup("luno");

		GroupInfo ginfo = srv.getGroup("toto");
		GroupInfo gi = srv.getGroup("luno");

		ginfo.addDevice("Gumichan01@GT-8189N", new DeviceData("192.245.2.1",
				2408));
		ginfo.addDevice("Miku@GT-24N", new DeviceData("192.245.2.6", 2048));
		ginfo.addDevice("Luka@I-8601t", new DeviceData("192.245.2.4", 2409));

		gi.addDevice("Alice@GT-8189N", new DeviceData("192.168.25.1", 1562));
		gi.addDevice("Bob@GT-24N", new DeviceData("192.168.25.2", 2142));
		gi.addDevice("David@shit", new DeviceData("192.168.25.3", 2106));
		gi.addDevice("Eloise@I-8601t", new DeviceData("192.168.25.4", 4058));

		ginfo = null;
		gi = null;

		Iterator<String> it = srv.getIterator();
		System.out.println("Groups");
		System.out.println(srv.nbGroups() + " groups");

		while (it.hasNext()) {

			GroupInfo g = srv.getGroup(it.next());
			Iterator<String> itg = g.getIterator();
			System.out.println(g.getName() + " - Devices");
			System.out.println(g.nbDevices() + " elements");

			while (itg.hasNext()) {
				System.out.println(g.getDevice(itg.next()).toString());
			}

		}
	}
	*/
}
