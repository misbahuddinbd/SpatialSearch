package echoknn;

import protocols.Coordinates;

public class prunepars{
	
	static prunepars instance;
	
	private Coordinates kcoordinates;
	private int kp;
	
	public prunepars(Coordinates kcoordinates, int kp){
		this.kcoordinates = kcoordinates;
		this.kp = kp;
	}

	public Coordinates getKcoordinates() {
		return kcoordinates;
	}

	public int getKp() {
		return kp;
	}

	public void setKcoordinates(Coordinates kcoordinates) {
		this.kcoordinates = kcoordinates;
	}

	public void setKp(int kp) {
		this.kp = kp;
	}

}
