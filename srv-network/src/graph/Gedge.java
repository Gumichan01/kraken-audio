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

	String[] recPath() {

		// System.out.println("rec: " + name + " | " + succ.toString());
		if (succ.isEmpty())
			return new String[] { name };

		else {

			ArrayList<String> lstring = new ArrayList<>();

			for (Gedge gdev : succ) {

				StringBuilder sb = new StringBuilder(name);
				String[] sarray = gdev.recPath();

				if (sarray == null)
					continue;

				for (String s : sarray)
					sb.append(" ").append(s);

				// System.out.println("rec loop: " + sb.toString());
				lstring.add(sb.toString());
			}

			String[] rpath = new String[lstring.size()];
			return lstring.toArray(rpath);
		}
	}

	@Override
	public String toString() {

		return "- " + name + " -";
	}
}
