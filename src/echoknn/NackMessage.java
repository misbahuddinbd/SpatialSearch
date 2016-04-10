package echoknn;

import peersim.core.Node;
import protocols.Coordinates;

public class NackMessage {
	
	private Node nacker;
	
	private Coordinates loc;
	
	private int k;
	
	public NackMessage(Node nacker, Coordinates loc, int k) {
		this.nacker = nacker;
		this.loc = loc;
		this.k = k;
 	}

	public Node getNacker() {
		return nacker;
	}
	
	public Coordinates getLoc(){
		return loc;
	}
	
	public int getK(){
		return k;
	}
}
