package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
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
		
		for(String s: ss) {
		
			vlist.add(graph.get(s).toString());
		}
		
		return vlist;
	}
	
	public ArrayList<String> getLines() {
		
		ArrayList<String> elist = new ArrayList<>();
		Set<String> ss = graph.keySet();

		for(String s: ss) {
			
			elist.add(graph.get(s).getLines());
		}

		return elist;
	}
	
	public ArrayList<String> getPaths(String gstr) {

		GVertex gdev = graph.get(gstr);
		System.out.println("graph paths: " + gstr);

		if (gdev == null)
			return null;

		// Stack that contains the edges already checked
		ArrayList<String> stack = new ArrayList<>();
		stack.add(gstr);

		List<String> lsucc = Arrays.asList(gdev.recPath(stack, null));
		//List<String> lpred = new ArrayList<>();
		//Set<String> ss = graph.keySet();

		// Clear the stack
		//stack.clear();

		/*for (String s : ss) {

			if (!s.equals(gstr)) {

				System.out.println("----------------");
				System.out.println("graph pred loop: " + s);
				stack.add(s);
				lpred.addAll(Arrays.asList(graph.get(s).recPath(stack, s)));
				stack.clear();
			}
		}*/

		System.out.println("graph succ: " + lsucc.toString());
		System.out.println("----------------");
		// System.out.println("graph pred: " + lpred.toString());

		ArrayList<String> paths = new ArrayList<>(lsucc);
		System.out.println("graph final: " + paths.toString());
		return paths;
	}

}
