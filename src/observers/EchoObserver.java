package observers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import utilities.FileIO;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import datastructures.GAPTableEntry;
import protocols.GT;
import datastructures.MBR;
import datastructures.Disc;
import protocols.EchoState;
import protocols.Invoke;

public class EchoObserver implements Control {

	public final int pid;
	private String outResultFile;
	private String outputFolder;
	private int netsize;
	private double radius;
	private int table_id;
	
	public EchoObserver(String prefix){
		this.pid = Configuration.getPid(prefix+"."+"protocol");	
		this.table_id = Configuration.getPid(prefix + "." + "table_protocol");
		this.outputFolder = Configuration.getString(prefix+"."+"folder");
		this.netsize = Configuration.getInt(prefix+"."+"netsize");
		this.radius = Configuration.getDouble(prefix+"."+"radius");
		File outputFolderFile = new File(outputFolder);
		if (!outputFolderFile.exists())
			outputFolderFile.mkdir();
		this.outResultFile = outputFolder + "/"+netsize+radius+".csv";
		FileIO.delete(outResultFile);
	}
	
	@Override
	public boolean execute() {
		
		System.out.println("Echo Observer: "+CommonState.getIntTime());
		
		SortedSet<Long> spatialSet = new TreeSet<Long>();
		SortedSet<Long> completeSet = new TreeSet<Long>();

		Disc querydisc = Invoke.getInstance().getQuery();
		Node invoker = Invoke.getInstance().getInvoker();
		Double radius = querydisc.getRadius();
		GT gi = (GT) invoker.getProtocol(this.table_id);
		Long invLevel = gi.getLevel();
		ArrayList<Node> verify = new ArrayList<Node>();
		
		for(int i = 0; i < Network.size(); i++){
			
			Node node = Network.get(i);
			GT gt = (GT) node.getProtocol(this.table_id);
			completeSet.add(gt.getLevel());
			EchoState es = (EchoState) node.getProtocol(this.pid);
			MBR nodembr = gt.getMbr();
			
			if(overlap(querydisc,nodembr)) verify.add(node);		
			
			if (es.isIs_root()){
				System.out.print(CommonState.getTime()-1+" "+Network.size()+" "+radius+" "+es.getAnswer().size()+" "+es.getVisited().size());
			
				for(Node n:es.getVisited()){
					GT gn = (GT) n.getProtocol(this.table_id);	
					spatialSet.add(gn.getLevel());
				}
				
				FileIO.append(""+radius+","+es.getVisited().size(), outResultFile);	
			}
			
			if (i==Network.size()-1) FileIO.append(","+verify.size(), outResultFile);		
			
		}
		
		EchoState esi = (EchoState) invoker.getProtocol(this.pid);
		GT gti = (GT) invoker.getProtocol(this.table_id);
		
	
		ArrayList<Long>spatialList = new ArrayList<Long>(spatialSet);
		ArrayList<Long>completeList = new ArrayList<Long>(completeSet);
		
		int indexInv = spatialList.indexOf(invLevel);
		int maxLevel = Math.max(indexInv-0, spatialList.size()-indexInv-1);
		int maxTreeLevel = Math.max(indexInv-0, completeList.size()-indexInv-1);
		
		FileIO.append(","+maxLevel+","+maxTreeLevel+"\n", outResultFile);
		System.out.println(" "+verify.size()+" "+maxLevel+" "+maxTreeLevel);
		
		System.out.println("Trouble!!!!!");
		
		/*System.out.println(invoker.getID());
		System.out.println(gti.getMbr());
		System.out.println(radius);
*/		
		
		for(Node n: verify)
			if (!esi.getAnswer().contains(n)){
				GT ngt = (GT) n.getProtocol(this.table_id);
				System.out.print(n.getID()+" ");
				//System.out.println(ngt.getMbr());
			}
			
		return false;
	}
	
public boolean overlap(Disc disc, MBR mbr){
		
		boolean intersect = false;
		
		Double discx = disc.getCoordinates().getX();
		Double discy = disc.getCoordinates().getY();
		Double radius = disc.getRadius();
		
		if (discx>=mbr.getMinX() && discx<=mbr.getMaxX())
			if (discy>=mbr.getMinY() && discy<=mbr.getMaxY())
				return true;
		
		Double xdist = Math.min(Math.abs(discx-mbr.getMinX()), Math.abs(discx-mbr.getMaxX()));
		Double ydist = Math.min(Math.abs(discy-mbr.getMinY()), Math.abs(discy-mbr.getMaxY()));
		Double distance = Math.sqrt((Math.pow(xdist, 2)+Math.pow(ydist, 2)));
		
		if (distance <= radius) intersect = true;
			
		return intersect;
}
	public void printTable(HashMap <Node, GAPTableEntry> table){
		for(Node key: table.keySet()){
			System.out.print(key.getID());
			System.out.print(" ");
			System.out.print(table.get(key).toString());
			System.out.println();
	}
}
	
}
