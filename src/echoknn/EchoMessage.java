package echoknn;

import java.util.ArrayList;
import java.util.HashMap;

import peersim.core.Node;
import protocols.Coordinates;

public class EchoMessage {
	
	private ArrayList<Node> visited;
	private ArrayList<Node> answer;
	private Node echoer;
	private Coordinates loc;
	private int k;
	
	public EchoMessage(ArrayList<Node> visited, ArrayList<Node> answer, Node echoer, Coordinates loc, int k) {
		this.visited = visited;
		this.answer = answer;
		this.echoer = echoer;
		this.loc = loc;
		this.k = k;
	}

	public ArrayList<Node> getVisited() {
		return this.visited;
	}

	public ArrayList<Node> getAnswer() {
		return this.answer;
	}
	
	public Node getEchoer() {
		return echoer;
	}

	public Coordinates getLoc() {
		return loc;
	}

	public int getK() {
		return k;
	}

	
}
