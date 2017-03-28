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

	String[] recPath(ArrayList<String> stack, String dstop) {

		System.out.println("rec: " + name + " | " + succ.toString());
		System.out.println("rec stack: " + stack.toString());
		
		if (succ.isEmpty() || (dstop != null && name.equals(dstop)) ) {

			System.out.println("fix point: " + name);
			return new String[] { name };

		} else {

			ArrayList<String> lstring = new ArrayList<>();

			for (Gedge gdev : succ) {

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

	@Override
	public String toString() {

		return "- " + name + " -";
	}
}
