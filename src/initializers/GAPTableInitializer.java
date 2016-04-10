package initializers;

import java.util.HashMap;

import datastructures.GAPTableEntry;
import datastructures.MBR;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import utilities.FileIO;
import protocols.Coordinates;
import protocols.GT;

public class GAPTableInitializer implements Control {

	public final int pid;
	public final int coord_id;
	public final int link_id;
	private final double bound;
	private final int networksize;
	private final String fileprefix;
	private final String foldername;
	
	
	public GAPTableInitializer(String prefix){
		
		pid = Configuration.getPid(prefix+"."+"protocol");
		coord_id = Configuration.getPid(prefix+"."+"coordinates");
		link_id = Configuration.getPid(prefix+"."+"linkable");
		bound = (double) (Configuration.getInt(prefix+"."+ "bound"));
        networksize = Configuration.getInt(prefix+"."+"networksize");
        fileprefix = Configuration.getString(prefix+"."+"fileprefix");
        foldername = Configuration.getString(prefix+"."+"foldername");
		
	}
	
	@Override
	public boolean execute() {
		
		Node root = Network.get(0);
		
		for(int i=0;i < Network.size();i++){
			
			Node node = Network.get(i);
			
			IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(link_id);
			Coordinates coordinates = (Coordinates) node.getProtocol(coord_id);
			GT nodegt = (GT) node.getProtocol(pid);
			
			init(node, root, neighborhood, coordinates, nodegt);
			
		}
		
		String filename = foldername+"/"+fileprefix+"-"+networksize+"-"+bound+".txt";
		
		String index = FileIO.read(filename);

		String[] localIndices = index.split("\n\n");
		
		for(String segment: localIndices){
			
			String[] lines = segment.split("\n");

			int count = 0;
			Node node = Network.get(Integer.parseInt(lines[0]));
			GT gt = (GT) node.getProtocol(pid);
			
			HashMap <Node, GAPTableEntry> table = gt.getTable();
			
			gt.setNode(node);
			
			for(String line : lines){
				if (count>0){
					String[] confline = line.split(" ");
					Node neighbor = Network.get(Integer.parseInt(confline[0]));
					String[] gtentry = confline[1].split(",");
					String status = gtentry[0].substring(1,gtentry[0].length());
					Long level = Long.parseLong(gtentry[1]);
					Double minx = Double.parseDouble(gtentry[2].substring(1, gtentry[2].length()));
					Double miny = Double.parseDouble(gtentry[3].substring(0, gtentry[3].length()-1));
					Double maxx = Double.parseDouble(gtentry[4].substring(1, gtentry[4].length()));
					Double maxy = Double.parseDouble(gtentry[5].substring(0, gtentry[5].length()-2));
					MBR mbr = new MBR(minx, miny, maxx, maxy);
					GAPTableEntry entry = new GAPTableEntry(status,level,mbr);
					
					if (node.equals(neighbor)){
						gt.setStatus(status);
						gt.setLevel(level);
						gt.setMbr(mbr);
						gt.setOldmbr(mbr);
					}
					else if(status.equals("parent"))
						gt.setParent(neighbor);
					table.put(neighbor, entry);
					}
				count++;
			}
			
			gt.setTable(table);
			gt.setMbrs(computeMBRS(node,table, link_id));
			
		}
		return false;
	}
	
	public HashMap<Node, MBR> computeMBRS(Node node, HashMap<Node, GAPTableEntry> table, int link_id){
		
		HashMap<Node, MBR> mbrs = new HashMap<Node, MBR> ();
		IdleProtocol neighborhood = (IdleProtocol) node.getProtocol(link_id);
		mbrs.put(node, table.get(node).getMbr());
		
		for(int i = 0; i < neighborhood.degree(); i++){
			Node neighbor = neighborhood.getNeighbor(i);
			MBR mbr = table.get(node).getMbr();
			
			
			for(Node n: table.keySet()){
				
				String status = table.get(n).getStatus();
				
				if (status.equals("parent") || status.equals("child")){
					if (!n.equals(neighbor)){
							mbr = aggregateMBR(mbr,table.get(n).getMbr());
					}
				}
			}
			mbrs.put(neighbor, mbr);
		}
		
		return mbrs;
	}
	
	public static void init(Node node, Node root, IdleProtocol neighborhood, Coordinates coordinates, GT nodegt){
		
		nodegt.setNode(node);
		nodegt.setParent(node);
		nodegt.setStatus("self");
		nodegt.setMbr(new MBR(coordinates));
		
		if (node.getID() == root.getID()) nodegt.setLevel(0L);
		else nodegt.setLevel(Long.MAX_VALUE);
		
		HashMap <Node, GAPTableEntry> nodeTable = new HashMap <Node, GAPTableEntry>();
		HashMap <Node, MBR> nodembrs = new HashMap <Node, MBR>();
		
		GAPTableEntry nodentry = new GAPTableEntry(nodegt.getStatus(), nodegt.getLevel(), nodegt.getMbr());
		
		nodeTable.put(node, nodentry);
		nodembrs.put(node, nodegt.getMbr());
		
		for(int j=0; j < neighborhood.degree();j++){
			Node row = neighborhood.getNeighbor(j);
			GAPTableEntry rowentry = new GAPTableEntry("peer",Long.MAX_VALUE, new MBR());
			nodeTable.put(row, rowentry);
			nodembrs.put(row, new MBR());
		}
		
		nodegt.setTable(nodeTable);
		nodegt.setMbrs(nodembrs);
	}
	
	public static MBR aggregateMBR(MBR mbr1, MBR mbr2){
		MBR mbr = new MBR();
		
		mbr.setMinX(Math.min(mbr1.getMinX(), mbr2.getMinX()));
		mbr.setMinY(Math.min(mbr1.getMinY(), mbr2.getMinY()));
		mbr.setMaxX(Math.max(mbr1.getMaxX(), mbr2.getMaxX()));
		mbr.setMaxY(Math.max(mbr1.getMaxY(), mbr2.getMaxY()));
		
		return mbr;	
	}	
	
}