package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class Graph {

	private Hashtable<String, Gedge> graph;

	public Graph() {

		graph = new Hashtable<>();
	}

	public boolean addEdge(String name) {

		try {

			System.out.println("graph addition: " + name);
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

		Gedge gsrc = graph.get(src);
		Gedge gdest = graph.get(dest);

		if (gsrc == null || gdest == null)
			return false;

		return gsrc.addSucc(gdest);
	}

	public boolean unlink(String src, String dest) {

		if (src == null || dest == null)
			return false;

		System.out.println("graph unlink: " + src + " | " + dest);

		Gedge gsrc = graph.get(src);
		Gedge gdest = graph.get(dest);

		if (gsrc == null || gdest == null)
			return false;

		return gsrc.removeSucc(gdest);
	}

	public ArrayList<String> getPaths(String gstr) {

		Gedge gdev = graph.get(gstr);
		System.out.println("graph paths: " + gstr);

		if (gdev == null)
			return null;

		ArrayList<String> paths = new ArrayList<>(Arrays.asList(gdev.recPath()));
		System.out.print(paths.toString());
		return paths;
	}

}
