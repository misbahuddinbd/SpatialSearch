package messages;

import java.util.ArrayList;

import peersim.core.Node;

public class EchoMessage {
	
	private ArrayList<Node> visited;
	private ArrayList<Node> answer;
	private Node echoer;
	private Double area;
	
	/*public EchoMessage(ArrayList<Node> visited, ArrayList<Node> answer, Node echoer) {
		this.visited = visited;
		this.answer = answer;
		this.echoer = echoer;
	}*/
	
	public EchoMessage(Double area, ArrayList<Node> answer, Node echoer) {
		this.area = area;
		this.answer = answer;
		this.echoer = echoer;
	}

	public ArrayList<Node> getVisited() {
		return this.visited;
	}

	public ArrayList<Node> getAnswer() {
		return this.answer;
	}
	
	public Double getArea() {
		return area;
	}

	public Node getEchoer() {
		return echoer;
	}

}
