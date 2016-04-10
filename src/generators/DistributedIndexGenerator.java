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

package generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import datastructures.GAPTableEntry;
import datastructures.MBR;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import protocols.Coordinates;
import protocols.GT;
import protocols.localMBR;
import utilities.FileIO;

public class DistributedIndexGenerator implements Control {
	
	private final int pid;

	private final int mbrpid;
	private final int indexpid;
	private final double bound;
	private final int networksize;
	private final String fileprefix;
	private final String foldername;
	
	public DistributedIndexGenerator(String prefix) {
        pid = Configuration.getPid(prefix + "." + "protocol");
        mbrpid = Configuration.getPid(prefix + "." + "mbrProtocol");
        indexpid = Configuration.getPid(prefix + "." + "indexProtocol");
        bound = (double) (Configuration.getInt(prefix+"."+ "bound"));
        networksize = Configuration.getInt(prefix+"."+"networksize");
        fileprefix = Configuration.getString(prefix+"."+"fileprefix");
        foldername = Configuration.getString(prefix+"."+"foldername");
    }

	public boolean execute() {
		
		Long begin = System.currentTimeMillis();	
		System.out.println("Costructing distributed local index:");
		String filename = foldername+"/"+fileprefix+"-"+networksize+"-"+bound+".txt";
		FileIO.delete(filename);
		
		Node root = Network.get(0);
		MBR globalmbr = new MBR();
		
		for(int i=0;i < Network.size();i++){
			
			Node node = Network.get(i);
			
			IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(pid);
			localMBR lMBR = (localMBR) node.getProtocol(mbrpid);
			GT nodegt = (GT) node.getProtocol(indexpid);
			
			init(node, root, neighborhood, lMBR, nodegt);
			if (i==0) globalmbr = nodegt.getMbr();
			else globalmbr = aggregateMBR(globalmbr, nodegt.getMbr());
		}
		
		System.out.println("Initialized in "+(System.currentTimeMillis()-begin)/1000+" seconds");
		
		Queue<Node> queue = new LinkedList<Node>();
		ArrayList<Node> inqueue = new ArrayList<Node>();
		
		queue.add(root);
		inqueue.add(root);
	
		while(!queue.isEmpty()){
			Node node = queue.remove();			
			GT nodegt = (GT) node.getProtocol(indexpid);
			HashMap<Node, GAPTableEntry> nodetable = nodegt.getTable();
			
			for(Node neighbor: nodetable.keySet()){
				GT neighborgt = (GT) neighbor.getProtocol(indexpid);
				GAPTableEntry neighborentry = nodetable.get(neighbor);
				
				if (neighborentry.getStatus().equals("self")) neighborentry.setLevel(nodegt.getLevel());
				else{
					if (nodegt.getParent().equals(neighbor)){
						neighborentry.setLevel(nodegt.getLevel()-1);
						neighborentry.setStatus("parent");
					}
					else if(neighborgt.getParent().equals(neighbor) && !neighbor.equals(root)){
						neighborgt.setParent(node);
						neighborentry.setStatus("child");
						neighborgt.setLevel(nodegt.getLevel()+1);
						neighborentry.setLevel(nodegt.getLevel()+1);
					}
					
					if (!inqueue.contains(neighbor))	{
						queue.add(neighbor);	
						inqueue.add(neighbor);
					}
				}
				nodetable.put(neighbor, neighborentry);
			}
			nodegt.setTable(nodetable);
		}

		System.out.println("Constructed spanning tree in "+(System.currentTimeMillis()-begin)/1000+" seconds");

		GT rootgt = (GT) root.getProtocol(indexpid);
			
		HashMap<Node, GAPTableEntry> roottable = rootgt.getTable();
			
		for(Node neighbor: roottable.keySet()){
				
			GAPTableEntry neighborentry = roottable.get(neighbor); 
			
			if (neighborentry.getStatus().equals("parent") || neighborentry.getStatus().equals("child")){	
				
				ArrayList<Node> subtree = new ArrayList<Node>();
				subtree.add(neighbor);
				
				MBR neighbormbr = childrenMBR(neighbor, indexpid);
				neighborentry.setMbr(neighbormbr);
				roottable.put(neighbor, neighborentry);
			}
		}
		
		queue.add(root);
		inqueue.add(root);
	
		while(!queue.isEmpty()){
			
			Node node = queue.remove();
			GT nodegt = (GT) node.getProtocol(indexpid);
			HashMap<Node, GAPTableEntry> nodetable = nodegt.getTable();
			HashMap<Node, MBR> nodembrs = computeMBRS(node, nodetable, pid);
			for(Node neighbor: nodetable.keySet()){
				String neighborstatus = nodetable.get(neighbor).getStatus();
				if (neighborstatus.equals("child")){		
					GT neighborgt = (GT) neighbor.getProtocol(indexpid);
					HashMap<Node,GAPTableEntry> neighbortable = neighborgt.getTable();
					GAPTableEntry nodeentry = neighbortable.get(node);
					nodeentry.setMbr(nodembrs.get(neighbor));
					neighbortable.put(node, nodeentry);
					neighborgt.setTable(neighbortable);
					queue.add(neighbor);
					
				}
			}
		}
		
		System.out.println("Constructed distributed local index in "+(System.currentTimeMillis()-begin)/1000+" seconds");
		
		for(int i=0;i<Network.size();i++){
			Node node = Network.get(i);
			FileIO.append(node.getID()+"\n", filename);
			GT nodegt = (GT) node.getProtocol(indexpid);
			checkIndexTables(node, indexpid, globalmbr);
			for(Node neighbor: nodegt.getTable().keySet()){
				FileIO.append(neighbor.getID()+" "+nodegt.getTable().get(neighbor).toString()+"\n", filename);
			}
			FileIO.append("\n", filename);
		}

		System.out.println("Distributed local index on a network graph of "+networksize+" nodes is created in "+(System.currentTimeMillis()-begin)/1000+" seconds!!!");
        return false;
    }
	
	
	public static MBR childrenMBR(Node node, int indexpid){
		
		GT nodegt = (GT) node.getProtocol(indexpid);
		MBR nodembr = nodegt.getMbr();
		HashMap<Node, GAPTableEntry> nodetable = nodegt.getTable();
		for(Node neighbor: nodetable.keySet()){
			GAPTableEntry neighborentry = nodetable.get(neighbor);
				if (neighborentry.getStatus().equals("child")){	
					MBR neighbormbr = childrenMBR(neighbor, indexpid);	
					nodembr = aggregateMBR(neighbormbr, nodembr);					
					neighborentry.setMbr(neighbormbr);
					nodetable.put(neighbor, neighborentry);
			}
		}
		return nodembr;
	}
	
	public static void init(Node node, Node root, IdleProtocol neighborhood, localMBR lMBR, GT nodegt){
		
		nodegt.setNode(node);
		nodegt.setParent(node);
		nodegt.setStatus("self");
		nodegt.setMbr(new MBR(lMBR.getLX(), lMBR.getLY(), lMBR.getHX(), lMBR.getHY()));
		
		if (node.getID() == root.getID()) nodegt.setLevel(0L);
		else nodegt.setLevel(Long.MAX_VALUE);
		
		HashMap <Node, GAPTableEntry> nodeTable = new HashMap <Node, GAPTableEntry>();
		HashMap <Node, MBR> nodembrs = new HashMap <Node, MBR>();
		
		GAPTableEntry nodentry = new GAPTableEntry(nodegt.getStatus(), nodegt.getLevel(), nodegt.getMbr());
		
		nodeTable.put(node, nodentry);
		nodembrs.put(node, nodegt.getMbr());
		for(int j=0; j < neighborhood.degree();j++){
			
			Node row = neighborhood.getNeighbor(j);
			GAPTableEntry rowentry = new GAPTableEntry("peer",Long.MAX_VALUE, new MBR());
			
			nodeTable.put(row, rowentry);
			nodembrs.put(row, new MBR());
		}
		
		nodegt.setTable(nodeTable);
		nodegt.setMbrs(nodembrs);
	}
	
	public HashMap<Node, MBR> computeMBRS(Node node, HashMap<Node, GAPTableEntry> table, int pid){
		
		HashMap<Node, MBR> mbrs = new HashMap<Node, MBR> ();
		mbrs.put(node, table.get(node).getMbr());
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(pid);
		
		for(int i = 0; i < neighborhood.degree(); i++){
			
			Node neighbor = neighborhood.getNeighbor(i);
			MBR neighbormbr = table.get(node).getMbr();
			
			for(Node n: table.keySet()){
				String status = table.get(n).getStatus();
				if (!status.equals("peer") && !status.equals("self")){
					if (!n.equals(neighbor)){
						neighbormbr = aggregateMBR(neighbormbr,table.get(n).getMbr());
					}
				}
			}
			mbrs.put(neighbor, neighbormbr);
		}
		return mbrs;
	}

	
	public static void checkIndexTables(Node node, int indexpid, MBR globalmbr){
		
		GT gt = (GT) node.getProtocol(indexpid);
		
		HashMap<Node, GAPTableEntry> nodetable = gt.getTable();
		
		MBR nodembr = gt.getMbr();
		
		for(Node neighbor: nodetable.keySet()){
			
			GT neighborgt = (GT) neighbor.getProtocol(indexpid);
			HashMap<Node, GAPTableEntry> neighbortable = neighborgt.getTable();
			
			String neighborstatus = nodetable.get(neighbor).getStatus();
			String nodestatus = neighbortable.get(node).getStatus();
				
			if(neighborstatus.equals("peer")){
				if(!nodestatus.equals("peer")){
					System.out.println("Problem!!!");
					System.out.println(node.getID()+" see "+neighbor.getID()+" as "+neighborstatus+", but "+neighbor.getID()+" see "+node.getID()+" as "+nodestatus);						
				}
			}
			else if(neighborstatus.equals("parent")){
				if(!nodestatus.equals("child")){
					System.out.println("Problem!!!");
					System.out.println(node.getID()+" see "+neighbor.getID()+" as "+neighborstatus+", but "+neighbor.getID()+" see "+node.getID()+" as "+nodestatus);						
				}
			}
			else if(neighborstatus.equals("child")){
				if(!nodestatus.equals("parent")){
					System.out.println("Problem!!!");
					System.out.println(node.getID()+" see "+neighbor.getID()+" as "+neighborstatus+", but "+neighbor.getID()+" see "+node.getID()+" as "+nodestatus);						
				}
			}
				
			if (neighborstatus.equals("parent") || neighborstatus.equals("child")){
				nodembr = aggregateMBR(nodembr, nodetable.get(neighbor).getMbr());
				}
				
			}
		if (compareMBR(globalmbr, nodembr)==false){
			System.out.println();
			System.out.println(node.getID()+" has incorrect distributed index!!!");
			System.out.println(node.getID()+" see global MBR as "+nodembr.toString());
			System.out.println("Global MBR actually is "+globalmbr);
		}	
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
