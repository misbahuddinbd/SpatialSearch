package messages;

import peersim.core.Node;

public class CrashMessage {

	private Node node;
	
	public CrashMessage(Node node){
		this.node = node;
	}
	
	public Node getNode() {
		return node;
	}
		
}
