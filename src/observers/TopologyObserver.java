package observers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import peersim.config.Configuration;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.graph.Graph;
import peersim.reports.GraphObserver;

public class TopologyObserver extends GraphObserver {

    private static int pid;
    private final int networksize;
    
   
    public TopologyObserver(String prefix) {
        super(prefix);
        System.out.println(prefix);
        
        pid = Configuration.getPid(prefix+"."+"protocol");
        networksize = Configuration.getInt(prefix+"."+"networksize");
        
    }
   
    public boolean execute() {

    	
    	ArrayList<Node> cluster = new ArrayList<Node>();
    	Queue<Node> stream = new LinkedList<Node>();

    	Node root = Network.get(0);
    	
    	cluster.add(root);
    	stream.add(root);
    	
    	while(stream.peek()!=null){
    		Node target = stream.poll();
    		IdleProtocol neighborhood = (IdleProtocol) target.getProtocol(pid);
    		for(int i = 0; i < neighborhood.degree(); i++){
    			Node neighbor = neighborhood.getNeighbor(i);
    			
    			if (!cluster.contains(neighbor)){
    				cluster.add(neighbor);
    				stream.add(neighbor);
    			}
    		}
    	}
    	
    	if (cluster.size()==networksize)
    		System.out.println("The network graph is connected!!!");
    	else
    		System.out.println("The network graph is not connected!!!");
    	        
        return false;
    }
   	
    
}