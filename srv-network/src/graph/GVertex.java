package graph;

import java.util.ArrayList;

public class GVertex {

	public static final String SEP = ",";
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

	public String[] recPath(ArrayList<String> stack, String dstop) {

		System.out.println("rec: " + name + " | " + succ.toString());
		System.out.println("rec stack: " + stack.toString());

		if (succ.isEmpty() || (dstop != null && name.equals(dstop))) {

			System.out.println("fix point: " + name);
			return new String[] { name };

		} else {

			ArrayList<String> lstring = new ArrayList<>();

			for (GVertex gdev : succ) {

				StringBuilder sb = new StringBuilder(name);

				if (stack.contains(gdev.name)) {
					// Loop
					System.out.println("loop detected on " + gdev.name);
				} else {

					// Add the current dev in the stack
					stack.add(gdev.name);
					String[] sarray = gdev.recPath(stack, dstop);
					// Remove the last dev in the stack (current)
					stack.remove(stack.size() - 1);

					if (sarray == null)
						continue;

					for (String s : sarray)
						sb.append(" ").append(s);

					System.out.println("rec loop: " + sb.toString());
					lstring.add(sb.toString());
				}
			}

			String[] rpath = new String[lstring.size()];
			return lstring.toArray(rpath);
		}
	}

	public String getEdges() {

		StringBuilder st = new StringBuilder(id + " ");

		for (int i = 0; i < succ.size(); i++) {

			st.append(succ.get(i) + (i == succ.size() - 1 ? "" : SEP));
		}

		System.out.println(st.toString());
		return st.toString();
	}

	@Override
	public String toString() {

		return id + " " + name;
	}
}
