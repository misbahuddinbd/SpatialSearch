package datastructures;

import java.util.ArrayList;

import peersim.core.Node;
import protocols.Coordinates;

public class State{
	
	private ArrayList<Node> visited;
	
	private ArrayList<Node> answer;
	
	Long maxlevel;
	
	Long treelevel;
	
	public State(String prefix) {
		this.visited = new ArrayList<Node>();
		this.answer = new ArrayList<Node>();
		this.maxlevel = Long.MAX_VALUE;
		this.treelevel = Long.MAX_VALUE;
	}
	
	public State() {
		this.visited = new ArrayList<Node>();
		this.answer = new ArrayList<Node>();
		this.maxlevel = Long.MAX_VALUE;
		this.treelevel = Long.MAX_VALUE;
	}
	
	public State(ArrayList<Node> visited, ArrayList<Node> answer,
			Long maxlevel, Long treelevel) {
		super();
		this.visited = visited;
		this.answer = answer;
		this.maxlevel = maxlevel;
		this.treelevel = treelevel;
	}

	public ArrayList<Node> getVisited() {
		return visited;
	}

	public void setVisited(ArrayList<Node> visited) {
		this.visited = visited;
	}

	public ArrayList<Node> getAnswer() {
		return answer;
	}

	public void setAnswer(ArrayList<Node> answer) {
		this.answer = answer;
	}

	public Long getMaxlevel() {
		return maxlevel;
	}

	public void setMaxlevel(Long maxlevel) {
		this.maxlevel = maxlevel;
	}

	public Long getTreelevel() {
		return treelevel;
	}

	public void setTreelevel(Long treelevel) {
		this.treelevel = treelevel;
	}
	
	
}
