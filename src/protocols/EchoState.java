package protocols;

import java.util.ArrayList;

import peersim.core.Node;
import peersim.core.Protocol;

public class EchoState implements Protocol {

	boolean is_root;
	boolean is_visited;
	Node parent;
	
	private ArrayList<Node> nodes;
	private ArrayList<Node> visited;
	private ArrayList<Node> answer;
	private Double totalArea;
	private Double resultArea;
	private Double answerDistance;
	private Double totalDistance;
	
	public EchoState(String prefix){
		
		this.is_root = false;
		this.is_visited = false;
		this.parent = null;
		this.nodes = new ArrayList<Node>();
		this.visited = new ArrayList<Node>();
		this.answer = new ArrayList<Node>();
		this.totalArea = null;
		this.resultArea = null;
		this.answerDistance = null;
		this.totalDistance = null;
	}

	public boolean isIs_root() {
		return this.is_root;
	}

	public void setIs_root(boolean is_root) {
		this.is_root = is_root;
	}

	public boolean isIs_visited() {
		return this.is_visited;
	}

	public void setIs_visited(boolean is_visited) {
		this.is_visited = is_visited;
	}

	public Node getParent() {
		return this.parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	public ArrayList<Node> getVisited() {
		return this.visited;
	}

	public void setAnswer(ArrayList<Node> answer) {
		this.answer = answer;
	}
	
	public ArrayList<Node> getAnswer() {
		return this.answer;
	}

	public void setVisited(ArrayList<Node> visited) {
		this.visited = visited;
	}
	
	public Double getTotalArea() {
		return totalArea;
	}

	public void setTotalArea(Double totalArea) {
		this.totalArea = totalArea;
	}

	public Double getResultArea() {
		return resultArea;
	}

	public void setResultArea(Double resultArea) {
		this.resultArea = resultArea;
	}
	
	public Double getAnswerDistance() {
		return answerDistance;
	}

	public void setAnswerDistance(Double answerDistance) {
		this.answerDistance = answerDistance;
	}

	public Double getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(Double totalDistance) {
		this.totalDistance = totalDistance;
	}

	public String getNodestoString(){
		String nodesasstring="";
		for(int i=0; i<this.nodes.size();i++){
			nodesasstring.concat(nodes.get(i).getID()+" ");
		}
		return nodesasstring;
	}
	
	public Object clone() {
        EchoState es = null;
        try {
            es = (EchoState) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return es;
    }
	
}
