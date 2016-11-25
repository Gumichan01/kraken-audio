package srv;

import java.util.Hashtable;


public class DirectoryServer {
	
	private Hashtable<String, GroupInfo> groups;
	
	public DirectoryServer(){
		groups = new Hashtable<>();
	}
	
}
