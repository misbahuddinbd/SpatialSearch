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


import peersim.config.Configuration;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

import utilities.FileIO;

public class BAGraphGenerator extends WireGraph {
    

	private final int pid;
    private final double bound;
	private final int networksize;
	private final String fileprefix;
	private final String foldername;

    public BAGraphGenerator(String prefix) {
        super(prefix);
        pid = Configuration.getPid(prefix+"."+"protocol");
        bound = (double) (Configuration.getInt(prefix+"."+ "bound"));
        networksize = Configuration.getInt(prefix+"."+"networksize");
        fileprefix = Configuration.getString(prefix+"."+"fileprefix");
        foldername = Configuration.getString(prefix+"."+"foldername");
    }

    public void wire(Graph g) {
    	
    	Long begin = System.currentTimeMillis();
    	String filename = foldername+"/"+fileprefix+"-"+networksize+"-"+bound+".txt";
    	FileIO.delete(filename);
    	
    	for(int i=0;i<Network.size();i++){

    		Node node = Network.get(i);
    		
    		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(pid);
    		
    		for(int j=0; j<neighborhood.degree();j++){
    			FileIO.append(node.getID()+","+neighborhood.getNeighbor(j).getID()+"\n", filename);
    		}
    		
    	}
	
    	System.out.println("BA graph with "+networksize+" nodes is created in "+(System.currentTimeMillis()-begin)/1000+" seconds!!!");
    }
    
   }
