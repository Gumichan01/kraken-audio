package srv;


import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Iterator;

import com.sun.net.httpserver.HttpServer;

public class DirectoryServer {

	// private static final int BUFFER_SIZE = 1024;
	private static final int SERVER_PORT = 8080;

	HttpServer server;
	private Hashtable<String, GroupInfo> groups;

	public DirectoryServer() throws Exception {
		groups = new Hashtable<String, GroupInfo>();

		server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
		server.createContext("/", new RunClient());
		server.setExecutor(null); // creates a default executor
		System.out.println("The server is running");
	}

	public void launch() throws Exception {

		server.start();
	}

	public void stop() {

		server.stop(0);
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

	// Uncomment this block in order to test the class
	public static void main(String[] args) throws Exception {

		// PRODUCTION CODE
		new DirectoryServer().launch();
	}

}
