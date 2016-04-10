package messages;

import datastructures.Disc;
import peersim.core.Node;

public class ExploreMessage {
	
	private Node explorer;
	
	private Disc disc;
	
	public ExploreMessage(Node explorer, Disc disc){
		this.explorer = explorer;
		this.disc = disc;
	}

	public Node getExplorer() {
		return explorer;
	}
	
	public Disc getDisc() {
		return disc;
	}

}
