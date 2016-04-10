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
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import protocols.Coordinates;
import utilities.FileIO;

public class CoordinateGenerator implements Control {
	
	private final int pid;
	private final double bound;
	private final int networksize;
	private final String fileprefix;
	private final String foldername;
	
	public CoordinateGenerator(String prefix) {
        pid = Configuration.getPid(prefix + "." + "protocol");
        bound = (double) (Configuration.getInt(prefix+"."+ "bound"));
        networksize = Configuration.getInt(prefix+"."+"networksize");
        fileprefix = Configuration.getString(prefix+"."+"fileprefix");
        foldername = Configuration.getString(prefix+"."+"foldername");
    }

	public boolean execute() {
		
		Long begin = System.currentTimeMillis();
		
		String filename = foldername+"/"+fileprefix+"-"+networksize+"-"+bound+".txt";
		
		FileIO.delete(filename);
        Node n = Network.get(0);
        Coordinates prot = (Coordinates) n.getProtocol(pid);
        prot.setX(0.5*bound);
        prot.setY(0.5*bound);        
        FileIO.append(n.getID()+":"+prot.getX()+","+prot.getY()+"\n", filename);
        
        for (int i = 1; i < Network.size(); i++) {
            n = Network.get(i);
            prot = (Coordinates) n.getProtocol(pid);
            prot.setX(CommonState.r.nextDouble()*bound);
            prot.setY(CommonState.r.nextDouble()*bound);
            FileIO.append(n.getID()+":"+prot.getX()+","+prot.getY()+"\n", filename);
        }

        System.out.println(networksize+" coordinates are generated in "+(System.currentTimeMillis()-begin)/1000+" seconds !!!");
        
        System.out.println();
        
        return false;
    }
}
