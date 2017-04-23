package srv;

import graph.Graph;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Iterator;

import com.sun.net.httpserver.HttpServer;

public class DirectoryServer {

	// private static final int SERVER_PORT = 80;
	private static final int SERVER_PORT = 8000;

	private HttpServer server;
	private Hashtable<String, GroupInfo> groups;
	// Graph is visible from any class in the package
	Graph graph;

	public DirectoryServer() throws Exception {
		groups = new Hashtable<String, GroupInfo>();
		graph = new Graph();

		server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
		server.createContext("/", new RunClient(this));
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
