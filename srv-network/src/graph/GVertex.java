package graph;

import java.util.ArrayList;

public class GVertex {

	public static final String SEP = "|";
	private static int idc = 0;

	private int id;
	private String name;
	private ArrayList<GVertex> succ;

	public GVertex(String s) {

		id = ++idc;
		name = s;
		succ = new ArrayList<>();
	}

	public boolean addSucc(GVertex g) {

		return g != null && (!succ.contains(g)) && succ.add(g);
	}

	public boolean removeSucc(GVertex g) {

		return g != null && (succ.contains(g)) && succ.remove(g);
	}

	@Override
	public boolean equals(Object o) {

		GVertex g = (GVertex) o;
		return id == g.id && name.equals(g.name);
	}

	// Get lines that connect the current vertex to the others
	public String getLines() {

		StringBuilder st = new StringBuilder(id + " ");

		for (int i = 0; i < succ.size(); i++) {

			st.append(succ.get(i).id + (i == succ.size() - 1 ? "" : SEP));
		}

		return st.toString();
	}

	@Override
	public String toString() {

		return id + " " + name;
	}
}
