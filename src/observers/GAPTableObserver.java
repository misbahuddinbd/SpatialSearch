 
package observers;

import java.util.HashMap;

import datastructures.GAPTableEntry;
import datastructures.MBR;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import protocols.Coordinates;
import protocols.GT;
import protocols.localMBR;

public class GAPTableObserver implements Control{

	private final int table_id;
	private final int coord_id;
	private final int link_id;
	private final int mbr_id;
	
    public GAPTableObserver(String prefix) {
  
        table_id = Configuration.getPid(prefix + "." + "table_protocol");
        coord_id = Configuration.getPid(prefix+"."+"coordinates");
        mbr_id = Configuration.getPid(prefix+"."+"mbrProtocol");
        link_id = Configuration.getPid(prefix+"."+"linkable");
	}

    public boolean execute() {
    	
    	
    	MBR globalmbr = new MBR();
    	Boolean globalstate = true;
    	
    	int totalmessage = 0;
    	
    	for(int i=0 ; i < Network.size();i++){
    		Node node = (Node) Network.get(i);
    		localMBR lMBR = (localMBR) node.getProtocol(mbr_id);
    		//Coordinates coordinates = (Coordinates) node.getProtocol(coord_id);
    		if (i==0) globalmbr = new MBR(lMBR.getLX(), lMBR.getLY(), lMBR.getHX(), lMBR.getHY());
    		else globalmbr = aggregateMBR(globalmbr,new MBR(lMBR.getLX(), lMBR.getLY(), lMBR.getHX(), lMBR.getHY()));
    	}
    	
    	System.out.println();
    	boolean tag = true;
    	
    	for(int i = 0; i < Network.size();i++){
    		Node node = (Node) Network.get(i);
    		GT gt = (GT) node.getProtocol(table_id);
    		totalmessage = totalmessage + gt.getMessagecount();
    		
    		HashMap<Node,GAPTableEntry> table = gt.getTable();
    		MBR localmbr = new MBR(gt.getMbr());	
    		
    		for(Node neighbor: table.keySet()){
    				
    				GAPTableEntry gte = table.get(neighbor);		
    				
    				if (!gte.getStatus().equals("self")){
    					GT ngt = (GT) neighbor.getProtocol(table_id);
    					if (gte.getStatus().equals("peer")){
    						if (!ngt.getTable().get(node).getStatus().equals("peer")){
    							tag = false;
    							System.out.print("PROBLEM: "); 
    							System.out.println("Node: "+node.getID()+" neighbor(mygaptable): "+neighbor.getID()+" status: "+gte.getStatus()+" me(neighbor gap table): "+ngt.getTable().get(node).getStatus());
    						}
    					}
    					else if (gte.getStatus().equals("parent")){
    						localmbr = aggregateMBR(localmbr,gte.getMbr());
    						if (!ngt.getTable().get(node).getStatus().equals("child")){
    							tag = false;
    							System.out.print("PROBLEM: "); 
    							System.out.println("Node: "+node.getID()+" neighbor(mygaptable): "+node.getID()+" status: "+gte.getStatus()+" me(neighbor gap table): "+ngt.getTable().get(node).getStatus());
    						}
    					}
    					else if (gte.getStatus().equals("child")){
    						localmbr = aggregateMBR(localmbr,gte.getMbr());
    						if (!ngt.getTable().get(node).getStatus().equals("parent")){
    							tag = false;
    							System.out.print("PROBLEM: "); 
    							System.out.println("Node: "+node.getID()+" neighbor(mygaptable): "+node.getID()+" status: "+gte.getStatus()+" me(neighbor gap table): "+ngt.getTable().get(node).getStatus());
    						}
    					}
    				}
    			}
    		
    			//System.out.println("GAPTableObserver >> Cycle: "+CommonState.getIntTime()+"; Msg: tag: "+tag+" GlobalMBR"+compareMBR(globalmbr,localmbr));
    			if (compareMBR(globalmbr,localmbr)){
    			
    				
    			}
    			else{
    				globalstate = false;
    				if (!compareMBR(globalmbr,localmbr)){
    					//System.out.println("GAPTableObserver >> Cycle: "+CommonState.getIntTime()+"; Msg: There is a problem in constructing distributed index for node "+node.getID());
    					System.out.println(globalmbr.toString());
    					System.out.println(localmbr.toString());
    				}
    			}
    		}
    	if (globalstate==true) System.out.println("GAPTableObserver >> Cycle: "+CommonState.getIntTime()+"; Msg: Index is correct!");
    	//System.out.println("GAPTableObserver >> Cycle: "+CommonState.getIntTime()+"; Msg: update messages: "+totalmessage);
    	return false;
    }
    
	public static MBR aggregateMBR(MBR mbr1, MBR mbr2){
		MBR mbr = new MBR();
		
		mbr.setMinX(Math.min(mbr1.getMinX(), mbr2.getMinX()));
		mbr.setMinY(Math.min(mbr1.getMinY(), mbr2.getMinY()));
		mbr.setMaxX(Math.max(mbr1.getMaxX(), mbr2.getMaxX()));
		mbr.setMaxY(Math.max(mbr1.getMaxY(), mbr2.getMaxY()));
		
		return mbr;	
	}	
	
	public static boolean compareMBR(MBR mbr1, MBR mbr2){
		boolean tag = false;
		
		if (mbr1.getMinX().equals(mbr2.getMinX()))
			if (mbr1.getMinY().equals(mbr2.getMinY()))
				if (mbr1.getMaxX().equals(mbr2.getMaxX()))
					if (mbr1.getMaxY().equals(mbr2.getMaxY()))
						tag = true;
		return tag;
	}
	
}
