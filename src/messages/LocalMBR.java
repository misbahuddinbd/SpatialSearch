package messages;

import peersim.core.Node;

import datastructures.MBR;

public class LocalMBR {

	private Node node;
	private MBR mbr;
	
	public LocalMBR(Node node, MBR mbr){
		this.node = node;
		this.mbr = mbr;	
	}
	
	public Node getNode() {
		return node;
	}

	public MBR getMbr() {
		return mbr;
	}
		
}
