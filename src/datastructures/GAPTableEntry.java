package datastructures;

import peersim.core.Node;

public class GAPTableEntry {

	private Node parent;
	private String status;
	private Long level;
	private MBR mbr;

	
	public GAPTableEntry() {
		this.status = null;
		this.level = Long.MAX_VALUE;
		this.mbr = new MBR();
		
	}
	
	public GAPTableEntry(String prefix) {
		this.status = null;
		this.level = Long.MAX_VALUE;
		this.mbr = new MBR();
	
	}
	
	public GAPTableEntry(String status, Long level, MBR mbr) {
		this.status = status;
		this.level = level;
		this.mbr = mbr;
	}
	
	

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getLevel() {
		return level;
	}

	public void setLevel(Long level) {
		this.level = level;
	}

	public MBR getMbr() {
		return mbr;
	}

	public void setMbr(MBR mbr) {
		this.mbr = mbr;
	}
	
	@Override
	public String toString(){
		return "<"+this.status+","+this.level+","+this.mbr.toString()+">";
	}
	
}
