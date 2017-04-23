package graph;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

public class Graph {

	private Hashtable<String, GVertex> graph;

	public Graph() {

		graph = new Hashtable<>();
	}

	public boolean addEdge(String name) {

		try {

			System.out.println("graph addition: " + name);
			graph.put(name, new GVertex(name));
			return true;

		} catch (NullPointerException ne) {

			ne.printStackTrace();
			return false;
		}
	}

	public boolean rmEdge(String name) {

		try {

			if (graph.containsKey(name)) {

				System.out.println("graph remove: " + name);
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

		if (src == null || dest == null)
			return false;

		System.out.println("graph link: " + src + " | " + dest);

		GVertex gsrc = graph.get(src);
		GVertex gdest = graph.get(dest);

		if (gsrc == null || gdest == null)
			return false;

		return gsrc.addSucc(gdest);
	}

	public boolean unlink(String src, String dest) {

		if (src == null || dest == null)
			return false;

		System.out.println("graph unlink: " + src + " | " + dest);

		GVertex gsrc = graph.get(src);
		GVertex gdest = graph.get(dest);

		if (gsrc == null || gdest == null)
			return false;

		return gsrc.removeSucc(gdest);
	}

	public ArrayList<String> getVertices() {

		ArrayList<String> vlist = new ArrayList<>();
		Set<String> ss = graph.keySet();

		for (String s : ss) {

			vlist.add(graph.get(s).toString());
		}

		return vlist;
	}

	public ArrayList<String> getLines() {

		ArrayList<String> elist = new ArrayList<>();
		Set<String> ss = graph.keySet();

		for (String s : ss) {

			elist.add(graph.get(s).getLines());
		}

		return elist;
	}
}
