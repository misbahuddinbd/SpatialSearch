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
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import protocols.Coordinates;
import utilities.FileIO;

public class CoordinateInitializer implements Control {
	
	private final int pid;
	private final double bound;
	private final int networksize;
	private final String fileprefix;
	private final String foldername;
	
	public CoordinateInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + "protocol");
        bound = (double) (Configuration.getInt(prefix+"."+ "bound"));
        networksize = Configuration.getInt(prefix+"."+"networksize");
        fileprefix = Configuration.getString(prefix+"."+"fileprefix");
        foldername = Configuration.getString(prefix+"."+"foldername");
    }

	public boolean execute() {
		
		String filename = foldername+"/"+fileprefix+"-"+networksize+"-"+bound+".txt";
		
		try {
			String[] conflines = FileIO.readLines(filename);
			
			int count = 0;
			for (String line: conflines){
				Node n = Network.get(Integer.parseInt(line.split(":")[0]));
				Coordinates prot = (Coordinates) n.getProtocol(pid);
				Double x = Double.parseDouble(line.split(":")[1].split(",")[0]);
				Double y = Double.parseDouble(line.split(":")[1].split(",")[1]);
				
				
				
				if (x>0 && y>0 && x<bound && y<bound){
					prot.setX(x);
					prot.setY(y);
					count++;
				}
				else{
					System.out.println("Coordinates for node "+n.getID()+" are out of the bounds!!!");
				}
			}
			if (networksize == count) System.out.println(networksize+" nodes are configured with coordinates within the bound [0.0, "+bound+"]");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        return false;
    }
}
