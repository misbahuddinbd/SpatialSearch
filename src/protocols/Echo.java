package protocols;

import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import datastructures.GAPTableEntry;
import datastructures.MBR;
import datastructures.Disc;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.transport.Transport;
import utilities.FileIO;

import java.io.File;
import java.util.ArrayList;

import messages.EchoMessage;
import messages.ExploreMessage;
import messages.NackMessage;

public class Echo implements CDProtocol, EDProtocol, Cloneable {

	private final int table_id;
	private final int link_id;
	private final int transport_id;
	private final int state_id;
	private final int coordinate_id;
	private String outResultFile;
	private String outputFolder;
	private int netsize;
	private double fraction;
	private double rate;
	private double radius;
	
	
	public Echo(String prefix) {
		this.table_id = Configuration.getPid(prefix + "." + "table_protocol");
		this.link_id = Configuration.getPid(prefix + "." + "linkable");
		this.transport_id = Configuration.getPid(prefix+"."+"transport");
		this.state_id = Configuration.getPid(prefix+"."+"state");
		this.coordinate_id = Configuration.getPid(prefix + "." + "coordinates");
		this.outputFolder = Configuration.getString(prefix+"."+"folder");
		this.netsize = Configuration.getInt(prefix+"."+"netsize");
		this.radius = Configuration.getDouble(prefix+"."+"radius");
		this.fraction = Configuration.getDouble(prefix+"."+"fraction");
		this.rate = Configuration.getDouble(prefix+"."+"rate");
		File outputFolderFile = new File(outputFolder);
		if (!outputFolderFile.exists())
			outputFolderFile.mkdir();
		this.outResultFile = outputFolder + "/"+netsize+"-"+fraction+"-"+rate+".csv";
		FileIO.delete(outResultFile);
	}

	@Override
	public void nextCycle(Node node, int pid) {
		
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(this.link_id);
		GT gt = (GT) node.getProtocol(this.table_id);
		Transport trans = (Transport) node.getProtocol(this.transport_id);	
		EchoState es = (EchoState) node.getProtocol(this.state_id);
		Disc querydisc = Invoke.getInstance().getQuery();
				
		if (node.equals(Invoke.getInstance().getInvoker())){
		
			//System.out.println("Echo >> Cycle: "+CommonState.getIntTime()+" ; Msg: Invoker: "+Invoke.getInstance().getInvoker().getID());
			es.setIs_root(true);
			es.setParent(node);
			initiate(node, querydisc, gt.getMbr());
					
			ExploreMessage exp = new ExploreMessage(node, querydisc);
			ArrayList <Node> nodes = new ArrayList<Node>();

			for (int i = 0; i < neighborhood.degree(); i++){
				Node neighbor = neighborhood.getNeighbor(i);
				GAPTableEntry gte = gt.getTable().get(neighbor);
				
				String status = gte.getStatus();
				if (status.equals("parent") || status.equals("child")){
					if (overlap(querydisc, gte.getMbr())){
						//System.out.println("Echo >> Cycle: "+CommonState.getIntTime()+" ; Msg: Node "+Invoke.getInstance().getInvoker().getID()+" is sending message to "+neighborhood.getNeighbor(i).getID());
						nodes.add(neighborhood.getNeighbor(i));
						trans.send(node, neighborhood.getNeighbor(i), exp, pid);					
					}
				}
			}
			es.setNodes(nodes);
		}
	}
	
	@Override
	public void processEvent(Node node, int pid, Object message) {
		
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(link_id);
		GT gt = (GT) node.getProtocol(this.table_id);
		Transport trans = (Transport) node.getProtocol(transport_id);	
		EchoState es = (EchoState) node.getProtocol(this.state_id);	
		
		if(message instanceof ExploreMessage){
			
			ExploreMessage exp = (ExploreMessage) message;
			Node explorer = exp.getExplorer();
			Disc querydisc = exp.getDisc();

			if (es.isIs_visited()){
				NackMessage nack = new NackMessage(node);
				trans.send(node, explorer, nack, pid);
			}
			else{
				es.setIs_visited(true);
				es.setParent(explorer);
				
				initiate(node, querydisc, gt.getMbr());
				ArrayList<Node> nodes = new ArrayList<Node>();
				ExploreMessage exp_send = new ExploreMessage(node, querydisc);
				
				for (int i = 0; i < neighborhood.degree(); i++){
					Node neighbor = neighborhood.getNeighbor(i);
					
					
					if(!neighbor.equals(es.getParent())){
						GAPTableEntry gte = gt.getTable().get(neighbor);
						String status = gte.getStatus();
						if(status.equals("parent") || status.equals("child")){
							
							if (overlap(querydisc, gte.getMbr())){
								nodes.add(neighborhood.getNeighbor(i));
								trans.send(node, neighborhood.getNeighbor(i), exp_send, pid);					
							}
						}
					}
				}
				es.setNodes(nodes);
				
				nodes = es.getNodes();
				
				if(nodes.isEmpty()){
					//EchoMessage echo = new EchoMessage(es.getVisited(), es.getAnswer(), node);
					EchoMessage echo = new EchoMessage(es.getResultArea(), es.getAnswer(), node);
					trans.send(node, es.getParent(), echo, pid);
				}		
			}
		}
		else if (message instanceof EchoMessage){
			
			EchoMessage echo = (EchoMessage) message;
			
			//echoaggregate(echo.getVisited(), echo.getAnswer(), node, echo.getEchoer());
			aggregate(echo.getArea(), echo.getAnswer(), node);
			es.getNodes().remove(echo.getEchoer());
			
			//EchoMessage echo_send = new EchoMessage(es.getVisited(), es.getAnswer(), node);
			EchoMessage echo_send = new EchoMessage(es.getResultArea(), es.getAnswer(), node);
			if (es.getNodes().isEmpty()){
				if (es.isIs_root()==false ){
					trans.send(node, es.getParent(), echo_send, pid);
				}
				else{
			
					Double totalArea = 0.0;
					Double resultArea = 0.0;
					
					for(int t=0; t<Network.size();t++){
						Node neighbor = Network.get(t);
						GT neighborgt = (GT) neighbor.getProtocol(table_id);
						MBR QueryMBR = new MBR(Invoke.getInstance().getQuery());
						totalArea = totalArea + intersect(QueryMBR,neighborgt.getMbr());
					}
					
					ArrayList<Node>verify = new ArrayList<Node>();
					ArrayList<Node>trouble = new ArrayList<Node>();
					
					Double missedDistance = Double.MAX_VALUE;
					Double totalDistance = Double.MIN_VALUE;
					/*Node invoker = Invoke.getInstance().getInvoker();
					GT invokergt = (GT) invoker.getProtocol(table_id);
					MBR invokermbr = (MBR) invokergt.getMbr();
					Coordinates invokercoordinate = new Coordinates();
					invokercoordinate.setX((invokermbr.getMinX()+invokermbr.getMaxX())/2);
					invokercoordinate.setY((invokermbr.getMinY()+invokermbr.getMaxY())/2);
*/
					for(int t=0;t<Network.size();t++){					
						Node neighbor = Network.get(t);
						GT neighborgt = (GT) neighbor.getProtocol(table_id);
						if (overlap(Invoke.getInstance().getQuery(),neighborgt.getMbr())){
							verify.add(neighbor);
							if (!es.getAnswer().contains(neighbor)){
								trouble.add(neighbor);
								MBR intersectmbr = intersectMBR(neighborgt.getMbr(), new MBR(Invoke.getInstance().getQuery()));
								//missedDistance = distance(Invoke.getInstance().getQuery(), intersectmbr)/Invoke.getInstance().getQuery().getRadius();
								missedDistance = distance(Invoke.getInstance().getQuery(), intersectmbr);
								
								
								
								
								//missedDistance = distanceCoordinateMBR(invokercoordinate, intersectmbr);
								//System.out.println(invokercoordinate.toString()+" "+intersectmbr.toString());
								//System.out.println(""+missedDistance);
								FileIO.append(""+missedDistance+"\n", outResultFile);
							}
								
						}
					}
					//System.out.println("Network: "+Network.size()+" Answer: "+es.getAnswer().size()+" Visited: "+es.getVisited().size()+" Verify: "+verify.size()+" Trouble: "+trouble.size());
					//for(Node troublenode: trouble){
					//	System.out.println(troublenode.getID());
					//}
					if (missedDistance.equals(Double.MAX_VALUE)){
						missedDistance = -100.0;
					}
					
					
					Double radius = Invoke.getInstance().getQuery().getRadius();
					//System.out.println("Echo >> Cycle: "+CommonState.getIntTime()+" ; Msg: result: "+es.getResultArea()+" total: "+totalArea+" missed distance: "+missedDistance/trouble.size()+" "+totalDistance/verify.size());
					System.out.println("Echo >> Cycle: "+CommonState.getIntTime()+" ; Msg: result: "+es.getResultArea()/totalArea);
					//FileIO.append(""+es.getResultArea()+","+totalArea+","+missedDistance/radius+"\n", outResultFile);
				}
			}
		}
		 
		else if(message instanceof NackMessage){
			
			NackMessage nack = (NackMessage) message;
			Node nacker = nack.getNacker();
			es.getNodes().remove(nacker);
			if(es.getNodes().isEmpty()){
				//EchoMessage echo = new EchoMessage(es.getVisited(), es.getAnswer(), node);
				EchoMessage echo = new EchoMessage(es.getResultArea(), es.getAnswer(), node);
				trans.send(node, es.getParent(), echo, pid);
			}
		}
	}

	public void initiate(Node node, Disc disc, MBR mbr){
		
		EchoState es = (EchoState) node.getProtocol(this.state_id);
		Boolean status = overlap(disc, mbr);
		Double intersectarea = intersect(mbr, new MBR(disc));
		es.setResultArea(intersectarea);
		ArrayList<Node> initval = new ArrayList<Node>();
		//es.setVisited(initval);
		if (status==true) {
			initval.add(node);
		} 
		es.setAnswer(initval);

	}
	
	public void aggregate(Double childarea, ArrayList<Node> childAnswer, Node node){
		
		EchoState es = (EchoState) node.getProtocol(this.state_id);	
		es.setResultArea(es.getResultArea()+childarea);
		ArrayList<Node> nodeAnswer = es.getAnswer();
		
		if (nodeAnswer.size() < childAnswer.size()){
			for(int i=0;i<nodeAnswer.size();i++){
				if(!childAnswer.contains(nodeAnswer.get(i))){
					childAnswer.add(nodeAnswer.get(i));
				}
			}
			es.setAnswer(childAnswer);
		}
		else{
			for(int i=0;i<childAnswer.size();i++){
				if (!nodeAnswer.contains(childAnswer.get(i))){
					nodeAnswer.add(childAnswer.get(i));
				}
			}
			es.setAnswer(nodeAnswer);	
		}
	}
	
	public void echoaggregate(ArrayList<Node> childVisited, ArrayList<Node> childAnswer, Node node, Node childNode){
		EchoState es = (EchoState) node.getProtocol(this.state_id);	
		
		
		/*ArrayList<Node> nodeVisited = es.getVisited();
		
		if (nodeVisited.size() < childVisited.size()){
			for(int i=0;i<nodeVisited.size();i++){
				if(!childVisited.contains(nodeVisited.get(i))){
					childVisited.add(nodeVisited.get(i));
				}
			}
			es.setVisited(childVisited);
		}
		else{
			for(int i=0;i<childVisited.size();i++){
				if (!nodeVisited.contains(childVisited.get(i))){
					nodeVisited.add(childVisited.get(i));
				}
			}
			es.setVisited(nodeVisited);
		}
		
		ArrayList<Node> nodeAnswer = es.getAnswer();
		
		System.out.println(node.getID()+" "+childNode.getID()+" "+nodeAnswer.size()+" "+childAnswer.size());
		
		if (nodeAnswer.size() < childAnswer.size()){
			for(int i=0;i<nodeAnswer.size();i++){
				if(!childAnswer.contains(nodeAnswer.get(i))){
					childAnswer.add(nodeAnswer.get(i));
				}
			}
			es.setAnswer(childAnswer);
		}
		else{
			for(int i=0;i<childAnswer.size();i++){
				if (!nodeAnswer.contains(childAnswer.get(i))){
					nodeAnswer.add(childAnswer.get(i));
				}
			}
			es.setAnswer(nodeAnswer);		
		}*/
	}
	
	public Double distanceCoordinateMBR(Coordinates coordinate, MBR mbr){
		
			  Double dx = Math.max(Math.max(mbr.getMinX() - coordinate.getX(), 0), coordinate.getX() - mbr.getMaxX());
			  Double dy = Math.max(Math.max(mbr.getMinY() - coordinate.getY(), 0), coordinate.getY() - mbr.getMaxY());
			  return Math.sqrt(dx*dx + dy*dy);
			
	}
	
	
	public Double distance(Disc disc, MBR mbr){
		
		boolean intersect = false;
		
		Double discx = disc.getCoordinates().getX();
		Double discy = disc.getCoordinates().getY();
		Double radius = disc.getRadius();
		Double distancex = null;
		Double distancey = null;
		
		// 1
		if (discx<=mbr.getMinX() && discy>=mbr.getMaxY()){
			distancex = Math.abs(mbr.getMinX() - discx);
			distancey = Math.abs(mbr.getMaxY() - discy);
			
			//System.out.println("Quadrant 1");
		}
		// 2
		else if (discx>=mbr.getMinX() && discx<=mbr.getMaxX() && discy>=mbr.getMaxY()){
			distancex = 0.0;
			distancey = Math.abs(discy - mbr.getMaxY());
			//System.out.println("Quadrant 2");
		}
		// 3
		else if (discx>=mbr.getMaxX() && discy>=mbr.getMaxY()){
			distancex = Math.abs(discx - mbr.getMaxX());
			distancey = Math.abs(discy - mbr.getMaxY());
			//System.out.println("Quadrant 3");
		}
		// 4
		else if (discx<=mbr.getMinX() && discy>=mbr.getMinY() && discy<=mbr.getMaxY()){
			distancex = Math.abs(mbr.getMinX() - discx);
			distancey = 0.0;
			//System.out.println("Quadrant 4");
		}
		// 5
		else if (discx>=mbr.getMinX() && discx<=mbr.getMaxX() && discy>=mbr.getMinY() && discy<=mbr.getMaxY()){
			distancex = 0.0;
			distancey = 0.0;
			//System.out.println("Quadrant 5");
		}
		// 6
		else if (discx>=mbr.getMaxX() && discy>=mbr.getMinY() && discy<=mbr.getMaxY()){
			distancex = Math.abs(discx - mbr.getMaxX());
			distancey = 0.0;
			//System.out.println("Quadrant 6");
		}
		// 7
		if (discx<=mbr.getMinX() && discy<=mbr.getMinY()){
				distancex = Math.abs(mbr.getMinX() - discx);
				distancey = Math.abs(mbr.getMinY() - discy);
				//System.out.println("Quadrant 7");
		}
		// 8
		else if (discx>=mbr.getMinX() && discx<=mbr.getMaxX() && discy<=mbr.getMinY()){
			distancex = 0.0;
			distancey = Math.abs(discy - mbr.getMinY());
			//System.out.println("Quadrant 8");
		}
		// 9
		else if (discx>=mbr.getMaxX() && discy<=mbr.getMinY()){
			distancex = Math.abs(discx - mbr.getMaxX());
			distancey = Math.abs(discy - mbr.getMinY());
			//System.out.println("Quadrant 9");
		}
		
		Double distance = Math.sqrt(Math.pow(distancex, 2) + Math.pow(distancey, 2));
		
		//if (distance<radius)
		//	return 0.0;
		//else
			return distance;
		}

	
	
	public boolean overlap(Disc disc, MBR mbr){
	
	boolean intersect = false;
	
	Double discx = disc.getCoordinates().getX();
	Double discy = disc.getCoordinates().getY();
	Double radius = disc.getRadius();
	Double distancex = null;
	Double distancey = null;
	
	
	// 1
	if (discx<=mbr.getMinX() && discy>=mbr.getMaxY()){
		distancex = Math.abs(mbr.getMinX() - discx);
		distancey = Math.abs(mbr.getMaxY() - discy);
		//System.out.println("Quadrant 1");
	}
	// 2
	else if (discx>=mbr.getMinX() && discx<=mbr.getMaxX() && discy>=mbr.getMaxY()){
		distancex = 0.0;
		distancey = Math.abs(discy - mbr.getMaxY());
		//System.out.println("Quadrant 2");
	}
	// 3
	else if (discx>=mbr.getMaxX() && discy>=mbr.getMaxY()){
		distancex = Math.abs(discx - mbr.getMaxX());
		distancey = Math.abs(discy - mbr.getMaxY());
		//System.out.println("Quadrant 3");
	}
	// 4
	else if (discx<=mbr.getMinX() && discy>=mbr.getMinY() && discy<=mbr.getMaxY()){
		distancex = Math.abs(mbr.getMinX() - discx);
		distancey = 0.0;
		//System.out.println("Quadrant 4");
	}
	// 5
	else if (discx>=mbr.getMinX() && discx<=mbr.getMaxX() && discy>=mbr.getMinY() && discy<=mbr.getMaxY()){
		distancex = 0.0;
		distancey = 0.0;
		//System.out.println("Quadrant 5");
	}
	// 6
	else if (discx>=mbr.getMaxX() && discy>=mbr.getMinY() && discy<=mbr.getMaxY()){
		distancex = Math.abs(discx - mbr.getMaxX());
		distancey = 0.0;
		//System.out.println("Quadrant 6");
	}
	// 7
	if (discx<=mbr.getMinX() && discy<=mbr.getMinY()){
			distancex = Math.abs(mbr.getMinX() - discx);
			distancey = Math.abs(mbr.getMinY() - discy);
			//System.out.println("Quadrant 7");
	}
	// 8
	else if (discx>=mbr.getMinX() && discx<=mbr.getMaxX() && discy<=mbr.getMinY()){
		distancex = 0.0;
		distancey = Math.abs(discy - mbr.getMinY());
		//System.out.println("Quadrant 8");
	}
	// 9
	else if (discx>=mbr.getMaxX() && discy<=mbr.getMinY()){
		distancex = Math.abs(discx - mbr.getMaxX());
		distancey = Math.abs(discy - mbr.getMinY());
		//System.out.println("Quadrant 9");
	}
	
	if ((Math.pow(distancex, 2) + Math.pow(distancey, 2))<=(radius*radius))
		intersect = true;
	
	return intersect;
	
	}
	
	public Double intersect(MBR mbr1, MBR mbr2){
		
		Double x = Math.max(0, Math.min(mbr1.getMaxX(), mbr2.getMaxX()) - Math.max(mbr1.getMinX(),mbr2.getMinX()));
		Double y = Math.max(0, Math.min(mbr1.getMaxY(), mbr2.getMaxY()) - Math.max(mbr1.getMinY(), mbr2.getMinY()));
        return x*y;		
	}

	public MBR intersectMBR(MBR mbr1, MBR mbr2){
		
		Double minx = null;
		Double miny = null;
		Double maxx = null;
		Double maxy = null;
		
		
		if (mbr1.getMinX()>=mbr2.getMinX() && mbr1.getMinX()<=mbr2.getMaxX() && mbr1.getMaxX()>=mbr2.getMaxX()) {
			minx = mbr1.getMinX();
			maxx = mbr2.getMaxX();
		}
		else if (mbr1.getMinX()<=mbr2.getMinX() && mbr1.getMaxX()>=mbr2.getMinX() && mbr1.getMaxX()<=mbr2.getMaxX()){
			minx = mbr2.getMinX();
			maxx = mbr1.getMaxX();
		}
		else if (mbr1.getMinX()>=mbr2.getMinX() && mbr1.getMaxX()<=mbr2.getMaxX()){
			minx = mbr1.getMinX();
			maxx = mbr1.getMaxX();
		}
		else if (mbr1.getMinX()<=mbr2.getMinX() && mbr1.getMaxX()>=mbr2.getMaxX()){
			minx = mbr2.getMinX();
			maxx = mbr2.getMaxX();
		}
		
		if (mbr1.getMinY()>=mbr2.getMinY() && mbr1.getMinY()<=mbr2.getMaxY() && mbr1.getMaxY()>=mbr2.getMaxY()) {
			miny = mbr1.getMinY();
			maxy = mbr2.getMaxY();
		}
		else if (mbr1.getMinY()<=mbr2.getMinY() && mbr1.getMaxY()>=mbr2.getMinY() && mbr1.getMaxY()<=mbr2.getMaxY()){
			miny = mbr2.getMinY();
			maxy = mbr1.getMaxY();
		}
		else if (mbr1.getMinY()>=mbr2.getMinY() && mbr1.getMaxY()<=mbr2.getMaxY()){
			miny = mbr1.getMinY();
			maxy = mbr1.getMaxY();
		}
		else if (mbr1.getMinY()<=mbr2.getMinY() && mbr1.getMaxY()>=mbr2.getMaxY()){
			miny = mbr2.getMinY();
			maxy = mbr2.getMaxY();
		}
		
		return new MBR(minx, miny, maxx, maxy);
	}
	
		
	public Echo clone(){
		Echo ep = null;
		try{ ep = (Echo) super.clone(); }
		catch(CloneNotSupportedException e){ }
		return ep;
	}
}
