package echoknn;

import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import datastructures.GAPTableEntry;
import protocols.GT;
import datastructures.MBR;
import peersim.config.Configuration;
import peersim.core.IdleProtocol;
import peersim.core.Node;
import protocols.Coordinates;
import peersim.transport.Transport;
import echoknn.Invoke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Echo implements CDProtocol, EDProtocol, Cloneable {

	private final int table_id;
	private final int link_id;
	private final int transport_id;
	private final int state_id;
	private final int coordinate_id;

	
	public Echo(String prefix) {
		this.table_id = Configuration.getPid(prefix + "." + "table_protocol");
		this.link_id = Configuration.getPid(prefix + "." + "linkable");
		this.transport_id = Configuration.getPid(prefix+"."+"transport");
		this.state_id = Configuration.getPid(prefix+"."+"state");
		this.coordinate_id = Configuration.getPid(prefix+"."+"coord_protocol");
	}

	@Override
	public void nextCycle(Node node, int pid) {
		Transport trans = (Transport) node.getProtocol(this.transport_id);	
		EchoState es = (EchoState) node.getProtocol(this.state_id);
		
		if (node.equals(Invoke.getInstance().getInvoker())){
			
			Coordinates loc = Invoke.getInstance().getLoc();
			int k = Invoke.getInstance().getK();
			
			es.setIs_root(true);
			es.setIs_visited(true);
			es.setParent(node);

			initiate(node, loc, k);
			ArrayList<Node> answerprune = computePrunePars(node, new ArrayList<Node>(), loc, k);
			es.setNodes(prune(node, loc, k, answerprune));
			
			ExploreMessage exp = new ExploreMessage(node, loc, k, answerprune);
			
			for(Node n: es.getNodes()){
				trans.send(node, n, exp, pid);
			}
		}
	}
	
	@Override
	public void processEvent(Node node, int pid, Object message) {
		
		Transport trans = (Transport) node.getProtocol(transport_id);	
		EchoState es = (EchoState) node.getProtocol(this.state_id);	
		
		if(message instanceof ExploreMessage){

			ExploreMessage exp = (ExploreMessage) message;
			Node explorer = exp.getExplorer();
			Coordinates loc = exp.getLoc();
			int k = exp.getK();
			ArrayList<Node> answerprune = exp.getAnswerprune();

			if (es.isIs_visited()){
				NackMessage nack = new NackMessage(node, loc, k);
				trans.send(node, explorer, nack, pid);
			}
			else{
				es.setIs_visited(true);
				es.setParent(explorer);

				initiate(node, loc, k);
				ArrayList<Node> pp = computePrunePars(node, answerprune, loc, k);
				ArrayList<Node> nodes = new ArrayList<Node>();
				
				nodes = prune(node, loc, k, pp);
				
				if (nodes.contains(es.getParent())){
					nodes.remove(es.getParent());
				} 	
				es.setNodes(nodes);
				
				ExploreMessage exp_send = new ExploreMessage(node, loc, k, pp);
			
				for(Node neighbor: es.getNodes()){					
					trans.send(node, neighbor, exp_send, pid);
				}
				
				if(es.getNodes().isEmpty()){
					EchoMessage echo = new EchoMessage(es.getVisited(), es.getAnswer(), node, loc, k);
					trans.send(node, es.getParent(), echo, pid);
				}		
			}
		}
		else if (message instanceof EchoMessage){

			EchoMessage echo = (EchoMessage) message;
			Node echoer = echo.getEchoer();
			
			echoaggregate(echo.getVisited(), echo.getAnswer(), node, echo.getLoc(), echo.getK());
			
			ArrayList<Node> nodes = es.getNodes();
			nodes.remove(echoer);
			es.setNodes(nodes);
			
			if (es.getNodes().isEmpty()){
				if (es.isIs_root()==false ){
					EchoMessage echo_send = new EchoMessage(es.getVisited(), es.getAnswer(), node, echo.getLoc(), echo.getK());
					trans.send(node, es.getParent(), echo_send, pid);
				}
			}
		}
		
		else if(message instanceof NackMessage){
			
			NackMessage nack = (NackMessage) message;
			Node nacker = nack.getNacker();
			
			ArrayList<Node> nodes = es.getNodes();
			nodes.remove(nacker);
			es.setNodes(nodes);
						
			if(es.getNodes().isEmpty()){
				EchoMessage echo = new EchoMessage(es.getVisited(), es.getAnswer(), node, nack.getLoc(), nack.getK());
				trans.send(node, es.getParent(), echo, pid);
			}
		}
	}
	
	public void initiate(Node node, Coordinates loc, int k){
		
		EchoState es = (EchoState) node.getProtocol(this.state_id);
		
		ArrayList<Node> visited = new ArrayList<Node>();
		ArrayList<Node> candidates = new ArrayList<Node>();
		
		visited.add(node);
		candidates.add(node);
		
		es.setVisited(visited);		
		es.setAnswer(candidates);	
	}
	

	public void echoaggregate(ArrayList<Node> childVisited, ArrayList<Node> childAnswer, Node node, Coordinates loc, int k){
		
		EchoState es = (EchoState) node.getProtocol(this.state_id);	
		
		ArrayList<Node> nodeVisited = es.getVisited();
		for(int i=0;i<childVisited.size();i++){
			if (!nodeVisited.contains(childVisited.get(i)))
				nodeVisited.add(childVisited.get(i));
		}
		es.setVisited(nodeVisited);
		
		ArrayList<Node> candidates = es.getAnswer();
		for(int i=0;i<childAnswer.size();i++){
			if (!candidates.contains(childAnswer.get(i)))
				candidates.add(childAnswer.get(i));
		}
		
		es.setAnswer(knearest(candidates, loc, k));
	}
	
	private ArrayList<Node> prune(Node node, Coordinates loc, int k, ArrayList<Node> pp) {
		
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(this.link_id);
		GT gt = (GT) node.getProtocol(this.table_id);
				
		ArrayList <Node> nodes = new ArrayList<Node>();
		
		if (pp.size() < k){
    		for (int i = 0; i < neighborhood.degree(); i++){
    			Node neighbor = neighborhood.getNeighbor(i);
    			GAPTableEntry gte = gt.getTable().get(neighbor);
    		    if (gte.getStatus().equals("parent") || gte.getStatus().equals("child")){
    		    	nodes.add(neighbor);
    		    } 
    		}
    	}
    	else{
    		Coordinates farthest = kcoordinates(pp, loc);
    		for (int i = 0; i < neighborhood.degree(); i++){
    			Node neighbor = neighborhood.getNeighbor(i);
    			GAPTableEntry gte = gt.getTable().get(neighbor);
    		    if (gte.getStatus().equals("parent") || gte.getStatus().equals("child")){
    		    	if (geodistance(gte.getMbr(),loc)<=distance(farthest, loc)){
    					nodes.add(neighbor);
    				}
    		    }	
    		}	
    	}
		
		return nodes;
	}
	
	private ArrayList<Node> computePrunePars(Node node, ArrayList<Node> pp, Coordinates loc, int k){
		
		EchoState es = (EchoState) node.getProtocol(this.state_id);	
		pp.addAll(es.getAnswer());
		return knearest(pp, loc, k);
	}

	
	private Coordinates kcoordinates(ArrayList<Node> nodes, Coordinates loc) {
		
		Double kdist = Double.MIN_VALUE;
		Coordinates kcoordinate = new Coordinates(null);
		
		for(Node n : nodes){
			Coordinates coordinates = (Coordinates) n.getProtocol(this.coordinate_id);
			Double dist = distance(coordinates, loc);
			//System.out.println("kcoordinates: "+coordinates.toString()+" "+dist);
			if (kdist.equals(Double.MIN_VALUE) || dist>kdist){
				kdist = dist;
				kcoordinate = coordinates;
			}
		}
		return kcoordinate;
	}
		
	public double geodistance(MBR mbr, Coordinates loc){
		Double dx = Math.max(0, Math.max(mbr.getMinX()-loc.getX(), loc.getX() - mbr.getMaxX()));
		Double dy = Math.max(0, Math.max(mbr.getMinY()-loc.getY(), loc.getY() - mbr.getMaxY()));
		return Math.sqrt(Math.pow(dx, 2)+Math.pow(dy, 2));
	}

	private ArrayList<Node> knearest(ArrayList<Node> candidates, Coordinates loc, int k) {

		Map<Double, ArrayList<Node>> nodemap = new TreeMap<Double, ArrayList<Node>>();
		
		ArrayList<Node> nodes = new ArrayList<Node>(); 
		for (Node candidate: candidates){
			Double kdist = distance(loc, (Coordinates) candidate.getProtocol(this.coordinate_id));
			if (nodemap.containsKey(kdist)){
				nodes = nodemap.get(kdist);
				nodes.add(candidate);
				nodemap.put(kdist, nodes);
			}
			else{
				nodes = new ArrayList<Node>();
				nodes.add(candidate);
				nodemap.put(kdist, nodes);
			}
		}

		int fill = 0;
		
		ArrayList<Node> knearest = new ArrayList<Node>();
		
		for(Map.Entry<Double,ArrayList<Node>> entry : nodemap.entrySet()) {
			if (fill<=k){
				nodes = entry.getValue();
				if(nodes.size()<(k-fill)) {
					knearest.addAll(nodes);
					fill = fill + nodes.size();
				}
				else{
					for(int i = 0; i < k-fill; i++){
						knearest.add(nodes.get(i));
						fill=fill+1;
					}					
				}	
			}
		}		
		return knearest;
	}

	private Double distance(Coordinates ncoordinates, Coordinates loc){
		return Math.sqrt(Math.pow(ncoordinates.getX()-loc.getX(), 2)+Math.pow(ncoordinates.getY()-loc.getY(), 2));	
	}

	public void printTable(HashMap <Node, GAPTableEntry> table){
		for(Node key: table.keySet()){
		System.out.print(key.getID());
		System.out.print(" ");
		System.out.print(table.get(key).toString());
		System.out.println();
		}
	}
	
	public void printNodes(ArrayList<Node> nodes){
		for(Node n: nodes){
			System.out.print(n.getID()+" ");
		}
	}
	
	public Echo clone(){
		Echo ep = null;
		try{ ep = (Echo) super.clone(); }
		catch(CloneNotSupportedException e){ }
		return ep;
	}
}
