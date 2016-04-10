package echoknn;

import java.util.ArrayList;
import peersim.core.Node;
import peersim.core.Protocol;

public class EchoState implements Protocol {

	boolean is_root;
	boolean is_visited;
	Node parent;
	Node invoker;
	
	private ArrayList<Node> nodes;
	private ArrayList<Node> visited;
	private ArrayList<Node> answer;
	
	public EchoState(String prefix){
		
		this.is_root = false;
		this.is_visited = false;
		parent = null;
		this.nodes = new ArrayList<Node>();
		this.visited = new ArrayList<Node>();
		this.answer = new ArrayList<Node>();
		this.invoker = null;
		
		
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

	public String getNodestoString(){
		String nodesasstring="";
		for(int i=0; i<this.nodes.size();i++){
			nodesasstring.concat(nodes.get(i).getID()+" ");
		}
		return nodesasstring;
	}
	
	public Node getInvoker() {
		return invoker;
	}

	public void setInvoker(Node invoker) {
		this.invoker = invoker;
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
