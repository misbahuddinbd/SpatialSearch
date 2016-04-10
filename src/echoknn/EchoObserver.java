package echoknn;

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
import echoknn.Invoke;
import datastructures.GAPTableEntry;
import protocols.GT;

public class EchoObserver implements Control {

	public final int pid;
	public final int table_id;
	private String outResultFile;
	private String outputFolder;
	private int netsize;
	private int k;
	Invoke inv;
	
	public EchoObserver(String prefix){
		this.pid = Configuration.getPid(prefix+"."+"protocol");	
		this.table_id = Configuration.getPid(prefix+"."+"table_protocol");
		this.outputFolder = Configuration.getString(prefix+"."+"folder");
		this.netsize = Configuration.getInt(prefix+"."+"netsize");
		this.k = Configuration.getInt(prefix+"."+"k");
		File outputFolderFile = new File(outputFolder);
		if (!outputFolderFile.exists())
			outputFolderFile.mkdir();
		this.outResultFile = outputFolder + "/"+netsize+k+".csv";
		FileIO.delete(outResultFile);
	}
	
	@Override
	public boolean execute() {
		SortedSet<Long> spatialSet = new TreeSet<Long>();
		SortedSet<Long> completeSet = new TreeSet<Long>();
		Node invoker = Invoke.getInstance().getInvoker();
		int knearest = Invoke.getInstance().getK();
		
		for(int i = 0; i < Network.size(); i++){
			Node node = Network.get(i);
			
			EchoState es = (EchoState) node.getProtocol(this.pid);
			GT gt = (GT) node.getProtocol(this.table_id);
			completeSet.add(gt.getLevel());
			
			
			if (es.isIs_root()){
				System.out.print(CommonState.getTime()-2+" ");
				System.out.print(k+" ");
				System.out.print(es.getAnswer().size());
				System.out.print(" ");
				System.out.print(es.getVisited().size());
				System.out.print(" ");
				for(Node n:es.getVisited()){
					GT gn = (GT) n.getProtocol(this.table_id);	
					spatialSet.add(gn.getLevel());
				}
				
				FileIO.append(""+knearest+","+es.getVisited().size()+","+es.getAnswer().size(), outResultFile);
				
			}
		}
		GT gi = (GT) invoker.getProtocol(this.table_id);
		Long invLevel = gi.getLevel();
		ArrayList<Long>spatialList = new ArrayList<Long>(spatialSet);
		ArrayList<Long>completeList = new ArrayList<Long>(completeSet);
		int indexInv = spatialList.indexOf(invLevel);
		int maxLevel = Math.max(indexInv-0, spatialList.size()-indexInv-1);
		int maxTreeLevel = Math.max(indexInv-0, completeList.size()-indexInv-1);
		System.out.print(" "+maxLevel+" "+maxTreeLevel);
		FileIO.append(","+maxLevel+","+maxTreeLevel+"\n", outResultFile);
		
		System.out.println();	
		return false;
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
