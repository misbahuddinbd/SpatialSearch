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

package initializers;

import java.io.IOException;

import peersim.config.Configuration;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;
import utilities.FileIO;

public class GraphInitializer extends WireGraph {
    

    private final int networksize;
    private final Double bound;
    private final String fileprefix;
    private final String foldername;
    
    public GraphInitializer(String prefix) {
        super(prefix);
   
        networksize = Configuration.getInt(prefix+"."+"networksize");
        bound = Configuration.getDouble(prefix+"."+"bound");
        fileprefix = Configuration.getString(prefix+"."+"fileprefix");
        foldername = Configuration.getString(prefix+"."+"foldername");
    }

    public void wire(Graph g) {
    	
    	System.out.println("Graph size "+g.size());
    	
    	
    	String filename = foldername+"/"+fileprefix+"-"+networksize+"-"+bound+".txt";
    	try {
			String[] lines = FileIO.readLines(filename);
			
			for(String line: lines){
				
				String[] components = line.split(",");
				int i = Integer.parseInt(components[0]);
				int j = Integer.parseInt(components[1]);
				if (!g.isEdge(i,j)) g.setEdge(i,j);
					
    			if (!g.isEdge(j,i)) g.setEdge(j,i);    				
    			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
    }
    
   }
