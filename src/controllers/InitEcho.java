package controllers;

import java.util.ArrayList;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import protocols.Coordinates;
import protocols.EchoState;
import protocols.Invoke;

public class InitEcho implements Control {

	public final int pid;
	public final Double radius;
	public final Double bound;
	Invoke inv;
	public final int coord_id;
	
	public InitEcho(String prefix){
		pid = Configuration.getPid(prefix+"."+"protocol");	
		radius = Configuration.getDouble(prefix+"."+"radius");
		bound = Configuration.getDouble(prefix+"."+"bound");
		coord_id = Configuration.getPid(prefix+"."+"coord_protocol");
		inv = new Invoke();
	}
	
	@Override
	public boolean execute() {
		
		Node node = Network.get((int) (CommonState.r.nextDouble()*Network.size()));
		Invoke.getInstance().setInvoker(node);
		Coordinates coordinates = new Coordinates();
		coordinates.setX(CommonState.r.nextDouble()*bound);
		coordinates.setY(CommonState.r.nextDouble()*bound);
		//node = Network.get((int) (CommonState.r.nextDouble()*Network.size()));
		//Coordinates coordinates = (Coordinates) node.getProtocol(coord_id);
		Invoke.getInstance().setQuery(coordinates, radius);
		
		for(int i = 0; i < Network.size(); i++){
			node = Network.get(i);
			EchoState es = (EchoState) node.getProtocol(pid);
			
			es.setIs_root(false);
			es.setIs_visited(false);
			es.setParent(null);
			es.setNodes(new ArrayList<Node>());
			es.setVisited(new ArrayList<Node>());
			es.setAnswer(new ArrayList<Node>());
		}
		return false;
	}
}
