package datum;

public class GroupData {

	private String gname;
	private int nbdev;

	public GroupData(String name, int nb) {

		gname = name;
		nbdev = nb;
	}

	public String getName() {

		return gname;
	}

	public int getNumberOfDevices() {

		return nbdev;
	}

	public String toString() {

		return gname + " " + nbdev;
	}
}
