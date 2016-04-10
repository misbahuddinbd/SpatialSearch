package datastructures;

import peersim.core.Node;
import protocols.Coordinates;

public class Query{
	
	static Query instance;
	
	public static Query getInstance()
	{
		if (instance == null)
			instance = new Query();		
		return instance;		
	}
	
	private Node invoker;
	private Coordinates coordinates;
	private Double parameter;
	private String type;
	
	public Query(){
		super();
	}
	
	public void setInvoker(Node node){
		this.invoker = node;
	}
	
	public Node getInvoker(){
		return this.invoker;
	}

	public Coordinates getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	public Double getParameter() {
		return parameter;
	}

	public void setParameter(Double parameter) {
		this.parameter = parameter;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
