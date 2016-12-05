package srv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;

import clt.ClientDevice;

public class DirectoryServer {

	private static final int BUFFER_SIZE = 1024;

	ServerSocket srvsock;
	char[] buffer;
	private Hashtable<String, GroupInfo> groups;

	public DirectoryServer() {
		groups = new Hashtable<String, GroupInfo>();
		srvsock = null;
	}

	public void launch() {

		try {

			srvsock = new ServerSocket(2048);
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

	// / * Uncomment this block in order to test the class
	public static void main(String[] args) {

		// PRODUCTION CODE
		new DirectoryServer().launch();

		/** For testing the server (DO NOT DEPLOY THAT)
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new DirectoryServer().launch();
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("client");
				ClientDevice d = new ClientDevice("toto@21", "192.168.1.1",
						1536);
				d.createGroup("group1");
				System.out.println("client");

				System.out.println("done");
				new ClientDevice("alice@1", "192.168.1.2", 1536)
						.joinGroup("group1");
				new ClientDevice("bob@4", "192.168.1.3", 1536)
						.joinGroup("group1");
				d.quitGroup("group1");
				d.close();
			}
		}).start();

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
}
