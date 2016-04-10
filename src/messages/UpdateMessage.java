package messages;

import peersim.core.Node;

import datastructures.MBR;

import java.util.HashMap;

public class UpdateMessage {

	private Node node;
	private Node parent;
	private Long level;
	private HashMap<Node, MBR> mbrs;
	private String msg;
	
	public UpdateMessage(Node node, Node parent, Long level, HashMap<Node, MBR> mbrs, String msg){
		this.node = node;
		this.parent = parent;
		this.level = level;
		this.mbrs = mbrs;	
		this.msg = msg;
	}
	
	public Node getNode() {
		return node;
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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
