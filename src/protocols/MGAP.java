package protocols;


import java.util.HashMap;

import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.config.Configuration;
import peersim.transport.Transport;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.IdleProtocol;
import datastructures.LocalVector;
import datastructures.MBR;
import datastructures.GAPTableEntry;
import messages.UpdateMessage;

public class MGAP implements CDProtocol, EDProtocol, Cloneable {
	
	public int linkable_id;
	public int transport_id;    
	public int table_id;
	public int base;
	public int increment;
	
	public MGAP(String prefix) {
	
		this.linkable_id = Configuration.getPid(prefix+"."+"linkable");
		this.transport_id = Configuration.getPid(prefix+"."+"transport");
		this.table_id = Configuration.getPid(prefix+"."+"table_protocol");
		this.base = Configuration.getInt(prefix+"."+"base");
		this.increment = Configuration.getInt(prefix+"."+"increment");
	}

	@Override
	public void nextCycle(Node node, int pid) {
				
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(linkable_id);
		GT gt = (GT) node.getProtocol(this.table_id);
		Transport trans = (Transport) node.getProtocol(transport_id);
		
		if (!compareMBR(gt.getMbr(),gt.getOldmbr())){
			gt.setOldmbr(gt.getMbr());			
			gt.setMbrs(computeMBRS(node, gt.getTable()));
			HashMap<Node,Double> updateRates = gt.getUpdateRate();
			UpdateMessage update = new UpdateMessage(node, gt.getParent(), gt.getLevel(), gt.getMbrs(), ""+node.getID());
			for(int i = 0; i < neighborhood.degree(); i++){
//				System.out.println("MGAP >> Cycle: "+CommonState.getIntTime()+"; Msg: "+node.getID()+" is sending message to "+neighborhood.getNeighbor(i).getID());
				Node neighbor = neighborhood.getNeighbor(i);
				Double updateRate = updateRates.get(neighbor);
				
				
				if (updateRate>0.0){
					updateRate--;
					updateRates.put(neighbor, updateRate);
					gt.setMessagecount(gt.getMessagecount()+1);
					trans.send(node, neighbor, update, pid);
				}
			}
			gt.setUpdateRate(updateRates);
		}
	}
	
	@Override
	public void processEvent(Node node, int pid, Object message) {
		
		Transport trans = (Transport) node.getProtocol(transport_id);
		GT gt = (GT) node.getProtocol(this.table_id);
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(linkable_id);
		
				
		if(message instanceof UpdateMessage){
			
			UpdateMessage update = (UpdateMessage) message;
//			System.out.println("MGAP >> Cycle: "+CommonState.getIntTime()+"; Msg: "+node.getID()+" received update "+update.getMsg());

			LocalVector vector = new LocalVector(gt.getParent(), gt.getLevel(), gt.getMbrs());
			gt.updateEntry(update.getNode(), update.getParent(), update.getLevel(), update.getMbrs());
			
			gt.restoreTableInvariant();
			
			gt.setMbrs(computeMBRS(node, gt.getTable()));
			
			LocalVector newvector = new LocalVector(gt.getParent(), gt.getLevel(), gt.getMbrs());
			
			
			if (!vectorCompare(vector, newvector)){	
					
				UpdateMessage newupdate = new UpdateMessage(node, gt.getParent(), gt.getLevel(), gt.getMbrs(), update.getMsg()+", "+node.getID());
				
				HashMap<Node,Double> updateRates = gt.getUpdateRate();
				
				for(int i = 0; i < neighborhood.degree(); i++){
//					System.out.println("MGAP >> Cycle: "+CommonState.getIntTime()+"; Msg: "+node.getID()+" is sending message to "+neighborhood.getNeighbor(i).getID());
					Node neighbor = neighborhood.getNeighbor(i);
					Double updateRate = updateRates.get(neighbor);
					if (updateRate>0){
						updateRate--;
						updateRates.put(neighbor, updateRate);
						gt.setMessagecount(gt.getMessagecount()+1);
						trans.send(node, neighborhood.getNeighbor(i), newupdate, pid);						
					}
				}
				gt.setUpdateRate(updateRates);
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
								if(!compareMBR(nte.getMbr(), new MBR())){
									if(compareMBR(mbrtarget,new MBR())){
										mbrtarget = nte.getMbr(); 
									}
									else{
										mbrtarget = aggregate(mbrtarget,nte.getMbr());										
									}
								}
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
		
		int keysize1 = mbrs1.keySet().size();
		int keysize2 = mbrs2.keySet().size();
		
		if (keysize1 == keysize2){
			for(Node key: mbrs1.keySet()){
				if (!compareMBR(mbrs1.get(key), mbrs2.get(key))){
					tag = false;
				}
			}
		}
		else tag = false;
			
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
	
	
	public MGAP clone(){
		MGAP gp = null;
		try{ gp = (MGAP) super.clone(); }
		catch(CloneNotSupportedException e){ }
		return gp;
	}
}
