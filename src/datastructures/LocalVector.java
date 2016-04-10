package datastructures;

import java.util.HashMap;

import peersim.core.Node;

public class LocalVector {
	
	private Node parent;
    private Long level;
    private HashMap<Node, MBR> mbrs;
    
    public LocalVector() {
		this.parent = null;
		this.level = null;
		this.mbrs = null;
	}	
    
	public LocalVector(Node parent, Long level, HashMap<Node, MBR> mbrs) {
		this.parent = parent;
		this.level = level;
		this.mbrs = mbrs;
	}	
	
	public Node getParent() {
		return parent;
	}

	public Long getLevel() {
		return level;
	}

	public HashMap<Node, MBR> getMbrs() {
		return mbrs;
	}

	@Override 
	public String toString(){
		return "Parent:"+this.parent.getID()+" Level:"+this.level;
	}
}