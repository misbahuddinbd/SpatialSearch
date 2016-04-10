package echoknn;

import java.util.ArrayList;

import peersim.core.Node;
import protocols.Coordinates;

public class ExploreMessage {
	
	private Node explorer;
	
	private Coordinates loc;
	
	private int k;
	
	private ArrayList<Node> answerprune;
		
	public ExploreMessage(Node explorer, Coordinates loc, int k, ArrayList<Node> answerprune){
		this.explorer = explorer;
		this.loc = loc;
		this.k = k;
		this.answerprune = answerprune;
	}
	
	public Node getExplorer() {
		return explorer;
	}
	
	public Coordinates getLoc() {
		return loc;
	}
	
	public int getK() {
		return k;
	}

	public ArrayList<Node> getAnswerprune() {
		return answerprune;
	}
	
}
