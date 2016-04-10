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
import peersim.config.Configuration;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;
import protocols.Coordinates;
import utilities.FileIO;

public class AKNNGraphGenerator extends WireGraph {
    
	private final int coordpid;
	private final int pid;
    private final int k;
    private final double bound;
	private final int networksize;
	private final String fileprefix;
	private final String foldername;

    public AKNNGraphGenerator(String prefix) {
        super(prefix);
        pid = Configuration.getPid(prefix+"."+"protocol");
        coordpid = Configuration.getPid(prefix+"."+"coordinateProtocol");
        k = Configuration.getInt(prefix+"."+"degree"); 
        bound = (double) (Configuration.getInt(prefix+"."+ "bound"));
        networksize = Configuration.getInt(prefix+"."+"networksize");
        fileprefix = Configuration.getString(prefix+"."+"fileprefix");
        foldername = Configuration.getString(prefix+"."+"foldername");
    }

    public void wire(Graph g) {
    	
    	Long begin = System.currentTimeMillis();
    	
    	String filename = foldername+"/"+fileprefix+"-"+networksize+"-"+bound+".txt";
    	FileIO.delete(filename);
    	Long linkCount = 0L;	
    	for (int i = 1; i < Network.size(); ++i) {
        	
        	Node node = Network.get(i);
        	
        	IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(pid);
        	
        	Double kdistance=0.0;
			Node knode = null;
			
			ArrayList<Node> topK = new ArrayList<Node>();
        	
			int cover = k - neighborhood.degree();
			
			for(int j = 0; j < Network.size();j++){
				
				Node neighbor = Network.get(j);
				IdleProtocol neighborNeighborhood = (IdleProtocol) neighbor.getProtocol(pid);
				if (node.equals(neighbor) || neighborhood.contains(neighbor) || (neighborNeighborhood.degree()>=k)) continue;
			
				if (topK.size()<cover){
					topK.add(neighbor);	
					
					if (topK.isEmpty()){
						knode = neighbor;
						kdistance = distance(node,neighbor,coordpid); 
					}
					else{
						Double neighbordistance = distance(node,neighbor,coordpid);
						if (neighbordistance > kdistance){
							knode = neighbor;
							kdistance = neighbordistance;
						}
					}					
				}
				else{
					
					Double neighbordistance = distance(node,neighbor,coordpid);
					
					if (neighbordistance < kdistance){
						int index = topK.indexOf(knode);
						topK.remove(index);
						topK.add(neighbor);
						knode = neighbor;
						kdistance = neighbordistance;
					}
				}	
			}
			
			for(Node n: topK){
				int a = (int) node.getID();
				int b =  (int) n.getID();
				if (!g.isEdge(a, b)) {
					g.setEdge(a, b);
					linkCount++;
					FileIO.append(a+","+b+"\n",filename);
				}
				if (!g.isEdge(b, a)) g.setEdge(b, a);
			}
			
		}
	
    	System.out.println("K(="+bound+")NN graph with "+networksize+" nodes and "+linkCount+" edges are created in "+(System.currentTimeMillis()-begin)/1000+" seconds!!!");
    }
    

    private static double distance(Node node1, Node node2, int coordpid) {
    	
    	Coordinates coordinates1 = (Coordinates) node1.getProtocol(coordpid);
    	Coordinates coordinates2 = (Coordinates) node2.getProtocol(coordpid);
        
        if (coordinates1.getX() == -1 || coordinates1.getY() == -1 || coordinates2.getX() == -1 || coordinates2.getY() == -1)
            throw new RuntimeException("Found un-initialized coordinate");
        return Math.sqrt(Math.pow((coordinates1.getX() - coordinates2.getX()),2) + Math.pow((coordinates1.getY() - coordinates2.getY()),2));
    }
    
    
   }
