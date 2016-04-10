/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package controllers;

import java.util.HashMap;

import datastructures.GAPTableEntry;
import datastructures.MBR;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;
import protocols.Coordinates;
import protocols.GT;
import protocols.localMBR;

public class BAGraphExtender extends WireGraph {

    private final int pid;
    private static int numnodes;
	private static double bound;
	private static double delta;
	private static int coord_id;
	private static int mbr_id;
	private static int k;
	private static int base;
	private static int indexpid;
	
    public BAGraphExtender(String prefix) {
        super(prefix);
        
        pid = Configuration.getPid(prefix+"."+"protocol");
        numnodes = Configuration.getInt(prefix+"."+"nodes");
        bound = Configuration.getDouble(prefix+"."+"bound");
        delta = Configuration.getDouble(prefix+"."+"delta");
        mbr_id = Configuration.getPid(prefix+"."+"mbrProtocol");
        coord_id = Configuration.getPid(prefix+"."+"coord_protocol");
        k = Configuration.getInt(prefix+"."+"k");
        base = Configuration.getInt(prefix+"."+"base");
        indexpid = Configuration.getPid(prefix+"."+"index");
       
     }

    public void wire(Graph g) {
		System.out.println("BAGraphExtender >> Cycle: "+CommonState.getIntTime()+"; Msg: graph extended by: "+numnodes+" nodes");

    	for (int i = 0; i < numnodes; ++i) {
    		
    		Node minnode = null;
    		Long minlevel = Long.MAX_VALUE;
    		
    		int origin = Network.size()-(i+1);
    		Node newnode = Network.get(origin);
    		
    		IdleProtocol neighborhood = (IdleProtocol) newnode.getProtocol(pid);
			
    		localMBR lMBR = (localMBR) newnode.getProtocol(mbr_id);
    		
    		Double x = CommonState.r.nextDouble()*bound;
            Double y = CommonState.r.nextDouble()*bound;
            
            Double lx = x - delta;
            Double ly = y - delta;
            Double hx = x + delta;
            Double hy = y + delta;
            
            if (lx<0) lMBR.setLX(0.0);
            else lMBR.setLX(lx);
            if (ly<0) lMBR.setLY(0.0);
            else lMBR.setLY(ly);
            if (hx>bound) lMBR.setHX(bound);
            else lMBR.setHX(hx);
            if (hy>bound) lMBR.setHY(bound);
            else lMBR.setHY(hy);
    		
            GT newnodegt = (GT) newnode.getProtocol(indexpid);
            HashMap<Node, GAPTableEntry> newnodetable = new HashMap<Node, GAPTableEntry>();
    		
    		for(int j=0;j<k;j++){ 
    			
	            int target = CommonState.r.nextInt(base);
	            Node targetnode = (Node) Network.get(target);
	            IdleProtocol targetneighborhood = (IdleProtocol) targetnode.getProtocol(pid);
	            
	            neighborhood.addNeighbor(targetnode);
	            g.setEdge(origin, target);
	            targetneighborhood.addNeighbor(newnode);        
	            g.setEdge(target, origin);
	            
    			GT targetgt = (GT) targetnode.getProtocol(indexpid);
	            HashMap<Node, GAPTableEntry> targetnodetable = targetgt.getTable();
	            GAPTableEntry targetentry = targetnodetable.get(targetnode);
	            Long targetlevel = targetentry.getLevel();
	            
	            if (targetlevel < minlevel){
	            	minlevel = targetlevel;
	            	minnode = targetnode;
	            }
	            
	            GAPTableEntry originentry = new GAPTableEntry("peer", Long.MAX_VALUE, new MBR());
	            targetnodetable.put(newnode, originentry);
	            
	            HashMap<Node, MBR> targetmbrs = targetgt.getMbrs();
	            
	            MBR originmbr = targetgt.getMbr();
	            
	            for(Node n: targetnodetable.keySet())
	            	if (!n.equals(newnode))
	            		if (targetnodetable.get(n).getStatus().equals("parent") || targetnodetable.get(n).getStatus().equals("child"))
	            			originmbr = aggregateMBR(originmbr,targetnodetable.get(n).getMbr());
	            
	            //targetmbrs.put(newnode, originmbr);
	            //targetgt.setMbrs(targetmbrs);
	            
	            newnodetable.put(targetnode, new GAPTableEntry("peer", Long.MAX_VALUE, new MBR()));
	        }  
    		
            newnodegt.setNode(newnode);
    		newnodegt.setParent(minnode);
    		newnodegt.setStatus("self");
    		newnodegt.setMbr(new MBR(lMBR.getLX(),lMBR.getLY(), lMBR.getHX(), lMBR.getHY()));
    		newnodegt.setOldmbr(new MBR());
    		newnodegt.setLevel(minlevel);
    		
    		GAPTableEntry newnodentry = new GAPTableEntry(newnodegt.getStatus(), newnodegt.getLevel(), newnodegt.getMbr());
            newnodetable.put(newnode, newnodentry);
    		
            newnodegt.setMbrs(computeMBRS(newnode, newnodetable, pid));
            
    		GAPTableEntry minnodentry = newnodetable.get(minnode);
    		minnodentry.setLevel(minlevel);
    		minnodentry.setStatus("parent");
    		newnodetable.put(minnode, minnodentry);
    		newnodegt.setTable(newnodetable);
    		
    		GT minnodegt = (GT) minnode.getProtocol(indexpid);
    		HashMap<Node, GAPTableEntry> minnodetable = minnodegt.getTable();
    		GAPTableEntry originentryminnode = minnodetable.get(newnode);
    		originentryminnode.setLevel(minlevel+1);
    		originentryminnode.setStatus("child");
    		minnodetable.put(newnode, originentryminnode);
    		
    	}
    	
    }
    
    public HashMap<Node, MBR> computeMBRS(Node node, HashMap<Node, GAPTableEntry> table, int pid){
		HashMap<Node, MBR> mbrs = new HashMap<Node, MBR> ();
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(pid);
		mbrs.put(node, table.get(node).getMbr());
		
		for(int i = 0; i < neighborhood.degree(); i++){
			Node neighbor = neighborhood.getNeighbor(i);
			MBR mbr = table.get(node).getMbr();
			for(Node n: table.keySet()){
				String status = table.get(n).getStatus();
					if (status != "peer" && status!="self")
						if (!n.equals(neighbor))
							mbr = aggregateMBR(mbr,table.get(n).getMbr());
			}
		}
		return mbrs;
	}
    
    public static MBR aggregateMBR(MBR mbr1, MBR mbr2){
		
    	MBR mbr = new MBR();
		mbr.setMinX(Math.min(mbr1.getMinX(), mbr2.getMinX()));
		mbr.setMinY(Math.min(mbr1.getMinY(), mbr2.getMinY()));
		mbr.setMaxX(Math.max(mbr1.getMaxX(), mbr2.getMaxX()));
		mbr.setMaxY(Math.max(mbr1.getMaxY(), mbr2.getMaxY()));
		
		return mbr;	
	}	
    
}
