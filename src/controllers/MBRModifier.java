package controllers;

import java.util.ArrayList;

import datastructures.MBR;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import protocols.Coordinates;
import protocols.EchoState;
import protocols.GT;
import protocols.Invoke;
import protocols.localMBR;

public class MBRModifier implements Control {

	public final int fraction;
	public final Double delta;
	public final Double bound;
	public final int mbr_id;
	public final int table_id;
	
	public MBRModifier(String prefix){
		mbr_id = Configuration.getPid(prefix+"."+"mbrProtocol");
		fraction = Configuration.getInt(prefix+"."+"fraction");
		delta = Configuration.getDouble(prefix+"."+"delta");
		bound = Configuration.getDouble(prefix+"."+"bound");
		table_id = Configuration.getPid(prefix+"."+"table_protocol");
	}
	
	@Override
	public boolean execute() {
		
		//int numnodes = (int) (Network.size()*fraction);
		int numnodes = fraction;
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		for(int i=0;i<numnodes;i++){
			Node candidate = Network.get((int) (CommonState.r.nextDouble()*Network.size()));
			nodes.add(candidate);
		}
		
		for(Node node: nodes){
			//System.out.println("MBRModifier >> Cycle: "+CommonState.getIntTime()+"; Msg: MBR changing for node: "+node.getID());
			localMBR lMBR = (localMBR) node.getProtocol(mbr_id);
			double lx = lMBR.getLX();
			double ly = lMBR.getLY();
			double hx = lMBR.getHX();
			double hy = lMBR.getLY();
			//System.out.println("Before: "+lMBR.toString());
			double d = Math.sqrt(Math.pow((hx-lx), 2) + Math.pow((hy-ly), 2));
			int coin = CommonState.r.nextInt()%2;
			int direction = CommonState.r.nextInt()%2;
			if (direction==0) direction = -1;
			if (coin==0){
				
				lx = lx + CommonState.r.nextDouble()*direction*delta;
				ly = ly + CommonState.r.nextDouble()*direction*delta;		
				if (lx<0) lx = 0;
				if (lx>bound) lx = bound;
				if (ly<0) ly = 0;
				if (ly>bound) ly = bound;
				lMBR.setLX(lx);
				lMBR.setLY(ly);
			}
			else{
				hx = hx + CommonState.r.nextDouble()*direction*delta;
				hy = hy + CommonState.r.nextDouble()*direction*delta;	
				if (hx<0) hx = 0;
				if (hx>bound) hx = bound;
				if (hy<0) hy = 0;
				if (hy>bound) hy = bound;
				lMBR.setHX(hx);
				lMBR.setHY(hy);
			}
			//System.out.println("After: "+lMBR.toString());
			GT gt = (GT) node.getProtocol(table_id);
			gt.setMbr(new MBR(lMBR.getLX(), lMBR.getLY(), lMBR.getHX(), lMBR.getHY()));

		}
		

		return false;
	}
}
