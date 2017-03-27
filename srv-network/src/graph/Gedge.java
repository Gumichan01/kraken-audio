package graph;

import java.util.ArrayList;

public class Gedge {

	private String name;
	private ArrayList<Gedge> succ;

	public Gedge(String s) {

		name = s;
		succ = new ArrayList<>();
	}

	public boolean addSucc(Gedge g) {

		return g != null && (!succ.contains(g)) && succ.add(g);
	}

	public boolean removeSucc(Gedge g) {

		return g != null && (succ.contains(g)) && succ.remove(g);
	}

	@Override
	public boolean equals(Object o) {

		Gedge g = (Gedge) o;
		return name.equals(g.name);
	}
}
