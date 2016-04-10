package protocols;

import datastructures.Disc;
import peersim.core.Node;

public class Invoke{
	
	static Invoke instance;
	
	public static Invoke getInstance()
	{
		if (instance == null)
			instance = new Invoke();		
		return instance;		
	}
	
	private Node invoker;
	private Disc query;
	
	public Invoke(){
		super();
	}
	
	public void setInvoker(Node node){
		this.invoker = node;
	}
	
	public Node getInvoker(){
		return this.invoker;
	}
	
	public void setQuery(Coordinates coordinates, Double radius){
		this.query = new Disc(coordinates, radius);
	}
	
	public Disc getQuery(){
		return this.query;
	}	
}
