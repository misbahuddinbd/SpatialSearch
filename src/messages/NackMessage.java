package messages;

import peersim.core.Node;

public class NackMessage {
	
	private Node nacker;
	
	public NackMessage(Node nacker) {
		this.nacker = nacker;
 	}

	public Node getNacker() {
		return nacker;
	}

}
