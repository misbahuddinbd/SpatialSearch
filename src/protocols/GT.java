package protocols;

import java.util.HashMap;

import datastructures.GAPTableEntry;
import datastructures.LocalVector;
import datastructures.MBR;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;

public class GT implements Protocol  {

    private Node node;
    private Node parent;
    private String status;
	private Long level;
	private MBR mbr;
	private MBR oldmbr;
	private int messagecount;	
	private HashMap <Node, MBR> mbrs;
	private HashMap <Node, GAPTableEntry> Table;
	private HashMap<Node, Double> updateRate;
	
	public GT(String prefix) {
        this.node = null;
        this.parent = null;
    	this.status = null;
		this.level = null;
		this.messagecount = 0;
		this.mbr = new MBR();
		this.mbrs = new HashMap <Node, MBR>();
		this.Table = new HashMap <Node, GAPTableEntry> ();
		this.updateRate = new HashMap<Node, Double>();
		
    }
	
	public int getMessagecount() {
		return messagecount;
	}

	public void setMessagecount(int messagecount) {
		this.messagecount = messagecount;
	}


	public void addEntry(Node n, String s, Long l, MBR m){
		this.Table.put(n, new GAPTableEntry(s, l, m));
	}

	public HashMap<Node, MBR> getMbrs() {
		return mbrs;
	}

	public void setMbrs(HashMap<Node, MBR> mbrs) {
		this.mbrs = mbrs;
	}
	
	public Node getNode() {
	    return node;
	}

	public void setNode(Node node) {
        this.node = node;
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
	
	public MBR getOldmbr() {
		return oldmbr;
	}

	public void setOldmbr(MBR oldmbr) {
		this.oldmbr = oldmbr;
	}

	public HashMap<Node, Double> getUpdateRate() {
		return updateRate;
	}

	public void setUpdateRate(HashMap<Node, Double> updateRate) {
		this.updateRate = updateRate;
	}

	public HashMap<Node, GAPTableEntry> getTable() {
		return Table;
	}

	public void setTable(HashMap<Node, GAPTableEntry> table) {
		Table = table;
	}
	
	public LocalVector getLocalVector(){
		return new LocalVector(this.parent, this.level, this.mbrs); 
	}
	
	public void printTable(){
		System.out.println("Node :"+this.node.getID());
		for(Node row: this.Table.keySet()){
			System.out.println(row.getID()+" "+this.Table.get(row).toString());
		}
	}
	
	public void printMBRS(){
		System.out.println("Node :"+this.node.getID());
		for(Node row: this.mbrs.keySet()){
			System.out.println(row.getID()+" "+this.mbrs.get(row).toString());
		}
	}
	
	public void updateEntry(Node node, Node parent, Long level, HashMap<Node, MBR> mbrs){
		if (this.node.equals(Network.get(0))){
			this.Table.put(node, new GAPTableEntry("child", this.level, mbrs.get(this.getNode())));
		}
		else{
			if(this.parent.equals(node))
				this.Table.put(node, new GAPTableEntry("parent", level, mbrs.get(this.getNode())));
			else if(this.node.equals(parent))
				this.Table.put(node, new GAPTableEntry("child", level, mbrs.get(this.getNode())));
			else
				this.Table.put(node, new GAPTableEntry("peer", level, mbrs.get(this.getNode())));
		}
	}
	
	public void restoreTableInvariant(){
		
		Long lowest_level = 100000L;
		Node lowest_node = null;
		if (!(this.node.equals(Network.get(0)))){
			for(Node key: this.Table.keySet()){
				GAPTableEntry lve = this.Table.get(key);
				if (!lve.getStatus().equals("self")){
					if (lve.getLevel() < lowest_level) {
						lowest_level = lve.getLevel();
						lowest_node = key;
					}
					else if (lve.getLevel() == lowest_level) {
						if (key.getID() < lowest_node.getID()){
							lowest_level = lve.getLevel();
							lowest_node = key;
						}
					}
				}
			}
		
			if (this.level > lowest_level){
			
				if (!this.node.equals(this.parent)){
					this.Table.get(this.parent).setStatus("peer");
					this.Table.put(this.parent, this.Table.get(this.parent));
				}
				
				this.parent = lowest_node;
				this.level = lowest_level+1;
				
				this.Table.get(this.getNode()).setLevel(this.level);
				this.Table.put(this.getNode(), this.Table.get(this.getNode()));
			
				this.Table.get(lowest_node).setStatus("parent");
				this.Table.put(lowest_node, this.Table.get(lowest_node));
			}
		}
	}
	
    public Object clone() {
        GT gt = null;
        try {
            gt = (GT) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return gt;
    }
    
}
