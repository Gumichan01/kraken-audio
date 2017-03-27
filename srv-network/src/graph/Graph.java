package graph;

import java.util.Hashtable;


public class Graph {

	private Hashtable<String, Gedge> graph;
	
	public Graph() {
		
		graph = new Hashtable<>();
	}
	
	public boolean addEdge(String name) {
		
		try {

			graph.put(name, new Gedge(name));
			return true;

		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return false;
		}
	}
	
	public boolean rmEdge(String name) {
		
		try {

			if (graph.containsKey(name)) {

				graph.remove(name);
				return true;
			}

			return false;

		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return false;
		}
	}
	
	public boolean link(String src, String dest) {
		
		if(src == null || dest == null)
			return false;
		
		System.out.println("graph: " + src + " | " + dest);
		
		Gedge gsrc = graph.get(src);
		Gedge gdest = graph.get(dest);
		
		if(gsrc == null || gdest == null)
			return false;
		
		return gsrc.addSucc(gdest);
	}

	public boolean unlink(String src, String dest) {
		
		if(src == null || dest == null)
			return false;
		
		System.out.println("graph: " + src + " | " + dest);
		
		Gedge gsrc = graph.get(src);
		Gedge gdest = graph.get(dest);
		
		if(gsrc == null || gdest == null)
			return false;
		
		return gsrc.removeSucc(gdest);
	}
	
}
