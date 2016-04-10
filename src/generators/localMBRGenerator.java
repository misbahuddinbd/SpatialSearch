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
import protocols.localMBR;
import utilities.FileIO;

public class localMBRGenerator implements Control {
	
	private final int pid;
	private final double bound;
	private final double delta;
	private final int networksize;
	private final String fileprefix;
	private final String foldername;
	
	
	public localMBRGenerator(String prefix) {
        pid = Configuration.getPid(prefix + "." + "protocol");
        bound = Configuration.getDouble(prefix+"."+ "bound");
        delta = Configuration.getDouble(prefix+"."+ "delta");
        networksize = Configuration.getInt(prefix+"."+"networksize");
        fileprefix = Configuration.getString(prefix+"."+"fileprefix");
        foldername = Configuration.getString(prefix+"."+"foldername");
    }

	public boolean execute() {
		
		Long begin = System.currentTimeMillis();
		
		String filename = foldername+"/"+fileprefix+"-"+networksize+"-"+bound+".txt";
		
		FileIO.delete(filename);
        Node n = Network.get(0);
        localMBR lMBR = (localMBR) n.getProtocol(pid);
        
        Double lx = (0.5*bound) - delta;
        Double ly = (0.5*bound) - delta;
        Double hx = (0.5*bound) + delta;
        Double hy = (0.5*bound) + delta;
        
        if (lx<0) lMBR.setLX(0.0);
        else lMBR.setLX(lx);
        if (ly<0) lMBR.setLY(0.0);
        else lMBR.setLY(ly);
        if (hx>bound) lMBR.setHX(bound);
        else lMBR.setHX(hx);
        if (hy>bound) lMBR.setHX(bound);
        else lMBR.setHY(hy);
        
        System.out.println(n.getID()+":"+lMBR.getLX()+","+lMBR.getLY()+","+lMBR.getHX()+","+lMBR.getHY()+"\n");
        
        FileIO.append(n.getID()+":"+lMBR.getLX()+","+lMBR.getLY()+","+lMBR.getHX()+","+lMBR.getHY()+"\n", filename);
        
        for (int i = 1; i < Network.size(); i++) {
            n = Network.get(i);
            lMBR = (localMBR) n.getProtocol(pid);
            
            Double x = CommonState.r.nextDouble()*bound;
            Double y = CommonState.r.nextDouble()*bound;
            
            lx = x - delta;
            ly = y - delta;
            hx = x + delta;
            hy = y + delta;
            
            if (lx<0) lMBR.setLX(0.0);
            else lMBR.setLX(lx);
            if (ly<0) lMBR.setLY(0.0);
            else lMBR.setLY(ly);
            if (hx>bound) lMBR.setHX(bound);
            else lMBR.setHX(hx);
            if (hy>bound) lMBR.setHY(bound);
            else lMBR.setHY(hy);
                    
            FileIO.append(n.getID()+":"+lMBR.getLX()+","+lMBR.getLY()+","+lMBR.getHX()+","+lMBR.getHY()+"\n", filename);

        }

        System.out.println(networksize+" coordinates are generated in "+(System.currentTimeMillis()-begin)/1000+" seconds !!!");
        
        System.out.println();
        
        return false;
    }
}
