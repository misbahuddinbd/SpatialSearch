package echoknn;

import peersim.core.Node;
import protocols.Coordinates;

public class Invoke{
	
	static Invoke instance;
	
	public static Invoke getInstance()
	{
		if (instance == null)
			instance = new Invoke();		
		return instance;		
	}
	
	private Node invoker;
	private Coordinates loc;
	private int k;
	
	public Invoke(){
		super();
	}
	
	public void setInvoker(Node node){
		this.invoker = node;
	}
	
	public Node getInvoker(){
		return this.invoker;
	}
	
	public Coordinates getLoc() {
		return loc;
	}

	public void setLoc(Coordinates loc) {
		this.loc = loc;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

}
