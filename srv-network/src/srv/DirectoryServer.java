package srv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;

public class DirectoryServer {

	private static final int BUFFER_SIZE = 1024;
	private static final int SERVER_PORT = 8080;

	ServerSocket srvsock;
	char[] buffer;
	private Hashtable<String, GroupInfo> groups;

	public DirectoryServer() {
		groups = new Hashtable<String, GroupInfo>();
		srvsock = null;
	}

	public void launch() {

		try {

			srvsock = new ServerSocket(SERVER_PORT);
			buffer = new char[BUFFER_SIZE];

			System.out.println("Server @"
					+ srvsock.getInetAddress().getHostAddress() + " "
					+ srvsock.getLocalPort());

			while (true) {

				Socket socket = srvsock.accept();

				if (socket == null) {
					srvsock.close();
					break;
				}

				// Create a thread that handle the connection
				new Thread(new RunClient(this, socket)).start();
			}

		} catch (IOException e) {

			e.printStackTrace();
			System.exit(-1);

		} catch (SecurityException | NullPointerException se) {

			se.printStackTrace();

		} catch (Exception u) {

			System.err.println("UNKNOWN EXCEPTION");
			u.printStackTrace();
			throw u;
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

	// Uncomment this block in order to test the class
	public static void main(String[] args) {

	// PRODUCTION CODE
		new DirectoryServer().launch();
	}

}
