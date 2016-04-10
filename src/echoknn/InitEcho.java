package echoknn;

import java.util.ArrayList;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import echoknn.Invoke;
import protocols.Coordinates;

public class InitEcho implements Control {

	public final int pid;
	public final int k;
	public final int coord_id;
	public Invoke inv;
	
	public InitEcho(String prefix){
		pid = Configuration.getPid(prefix+"."+"protocol");	
		k = Configuration.getInt(prefix+"."+"k");
		coord_id = Configuration.getPid(prefix+"."+"coord_protocol");
	}
	
	@Override
	public boolean execute() {
		
		Node node = Network.get((int) (CommonState.r.nextDouble()*Network.size()));
		
		Invoke.getInstance().setInvoker(node);
		node = Network.get((int) (CommonState.r.nextDouble()*Network.size()));
		Coordinates coordinates = (Coordinates) node.getProtocol(coord_id);
		Invoke.getInstance().setLoc(coordinates);
		Invoke.getInstance().setK(k);
		
		for(int i = 0; i < Network.size(); i++){
			node = Network.get(i);
			EchoState es = (EchoState) node.getProtocol(pid);
			
			es.setIs_root(false);
			es.setIs_visited(false);
			es.setParent(null);
			es.setNodes(new ArrayList<Node>());
			es.setVisited(new ArrayList<Node>());
			es.setAnswer(new ArrayList<Node>());
			es.setInvoker(Invoke.getInstance().getInvoker());
		}
		return false;
	}
}
