package protocols;


import java.util.HashMap;

import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.config.Configuration;
import peersim.transport.Transport;
import peersim.core.Node;
import peersim.core.IdleProtocol;
import datastructures.LocalVector;
import datastructures.MBR;
import datastructures.GAPTableEntry;
import messages.UpdateMessage;


public class GAP implements CDProtocol, EDProtocol, Cloneable {
	
	public int linkable_id;
	public int transport_id;    
	public int table_id;
	
	public GAP(String prefix) {
		this.linkable_id = Configuration.getPid(prefix+"."+"linkable");
		this.transport_id = Configuration.getPid(prefix+"."+"transport");
		this.table_id = Configuration.getPid(prefix+"."+"table_protocol");
	}

	@Override
	public void nextCycle(Node node, int pid) {
		
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(linkable_id);
		GT gt = (GT) node.getProtocol(this.table_id);
		Transport trans = (Transport) node.getProtocol(transport_id);
		
		if (node.getID() == 0){
			for(int i = 0; i < neighborhood.degree(); i++){
				//UpdateMessage update = new UpdateMessage(node, gt.getParent(), gt.getLevel(), gt.getMbrs(), ""+node.getID(),(int) 0);
				UpdateMessage update = new UpdateMessage(node, gt.getParent(), gt.getLevel(), gt.getMbrs(),"");
				trans.send(node, neighborhood.getNeighbor(i), update, pid);
			}				
		}
	}
	
	@Override
	public void processEvent(Node node, int pid, Object message) {
		
		Transport trans = (Transport) node.getProtocol(transport_id);
		GT gt = (GT) node.getProtocol(this.table_id);
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(linkable_id);
				
		if(message instanceof UpdateMessage){
						
			UpdateMessage update = (UpdateMessage) message;
			LocalVector vector = new LocalVector(gt.getParent(), gt.getLevel(), gt.getMbrs());
			
			gt.updateEntry(update.getNode(), update.getParent(), update.getLevel(), update.getMbrs());
			
			gt.restoreTableInvariant();
			
			gt.setMbrs(computeMBRS(node, gt.getTable()));
			LocalVector newvector = new LocalVector(gt.getParent(), gt.getLevel(), gt.getMbrs());
			
			if (!vectorCompare(vector, newvector)){	
				UpdateMessage newupdate = new UpdateMessage(node, gt.getParent(), gt.getLevel(), gt.getMbrs(),"");
				for(int i = 0; i < neighborhood.degree(); i++){
					trans.send(node, neighborhood.getNeighbor(i), newupdate, pid);
				}
			}	
		}	
	}
	
	
	public HashMap<Node, MBR> computeMBRS(Node node, HashMap<Node, GAPTableEntry> Table){
		HashMap<Node, MBR> mbrs = new HashMap<Node, MBR> ();
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(linkable_id);
		MBR mbrorigin = Table.get(node).getMbr();
		
		mbrs.put(node, mbrorigin);
		
		for(int i = 0; i < neighborhood.degree(); i++){
			MBR mbrtarget = Table.get(node).getMbr();
			Node target = neighborhood.getNeighbor(i);
			GAPTableEntry gte = Table.get(target);
			
			if (!gte.getStatus().equals("peer")){
				for(int j = 0; j < neighborhood.degree(); j++){
					Node candidate = neighborhood.getNeighbor(j);
					GAPTableEntry nte = Table.get(candidate);
					if (!target.equals(candidate)){
							if (nte.getStatus().equals("parent") || nte.getStatus().equals("child")){
								mbrtarget = aggregate(mbrtarget,nte.getMbr());
						}
					}
				 }
			}
			
			mbrs.put(target, mbrtarget);
		}
		return mbrs;
	}
	
	public boolean compareMBRS(HashMap<Node, MBR> mbrs1, HashMap<Node, MBR> mbrs2){
		
		boolean tag = true;
		for(Node key: mbrs1.keySet()){
			if (!compareMBR(mbrs1.get(key), mbrs2.get(key))){
				tag = false;
			}
		}
		return tag;
	}
	
	public boolean compareMBR(MBR mbr1, MBR mbr2){
		
		boolean tag = false;
		if (mbr1.getMinX().equals(mbr2.getMinX()))
			if (mbr1.getMinY().equals(mbr2.getMinY()))
				if (mbr1.getMaxX().equals(mbr2.getMaxX()))
					if (mbr1.getMaxY().equals(mbr2.getMaxY()))
						tag = true;
		return tag;
	}
	
	public MBR aggregate(MBR mbr1, MBR mbr2){
		MBR mbr;
		if (compareMBR(mbr1, new MBR(0.0, 0.0, 0.0, 0.0)))
			if (compareMBR(mbr2, new MBR(0.0, 0.0, 0.0, 0.0)))
				mbr = new MBR(0.0, 0.0, 0.0, 0.0);
			else
				mbr = mbr2;
		else
			if (compareMBR(mbr2, new MBR(0.0, 0.0, 0.0, 0.0)))
				mbr = mbr1;
			else
				mbr = new MBR(Math.min(mbr1.getMinX(), mbr2.getMinX()), Math.min(mbr1.getMinY(), mbr2.getMinY()), Math.max(mbr1.getMaxX(), mbr2.getMaxX()), Math.max(mbr1.getMaxY(), mbr2.getMaxY()));	
		return mbr;
	}
	
	public boolean vectorCompare(LocalVector oldvector, LocalVector newvector){

		boolean tag = false;
		
		if (oldvector.getParent().equals(newvector.getParent()))
			if (oldvector.getLevel().equals(newvector.getLevel()))
				if(compareMBRS(oldvector.getMbrs(), newvector.getMbrs()))
					tag = true; 
		return tag;
	}
    
	public void printTable(HashMap <Node, GAPTableEntry> table){
		for(Node key: table.keySet()){
		System.out.print(key.getID());
		System.out.print(" ");
		System.out.print(table.get(key).toString());
		System.out.println();
		}
	}
	
	public void printMBRS(HashMap <Node, MBR> mbrs){
		for(Node key: mbrs.keySet()){
		System.out.print(key.getID());
		System.out.print(" ");
		System.out.print(mbrs.get(key).toString());
		System.out.println();
		}
	}
	
	
	public GAP clone(){
		GAP gp = null;
		try{ gp = (GAP) super.clone(); }
		catch(CloneNotSupportedException e){ }
		return gp;
	}
}
