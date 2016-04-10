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

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import protocols.GT;

public class InitGTMessageCounter implements Control {

    private static int indexpid;
	
    public InitGTMessageCounter(String prefix) {
        indexpid = Configuration.getPid(prefix+"."+"index");
     }

    
	@Override
	public boolean execute() {
		
		
		for (int i = 0; i < Network.size(); ++i) {
			Node node = Network.get(i);
			GT nodegt = (GT) node.getProtocol(indexpid);
            nodegt.setMessagecount(0);
           
    	}
		
		
		
		
		return false;
	}
    
    
    
}
