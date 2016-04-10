package datastructures;

import protocols.Coordinates;

public class Disc{
	
	private Coordinates coordinates;
	private Double radius;
	
	public Disc(Coordinates coordinates, Double radius){
		this.coordinates = coordinates;
		this.radius = radius;
	}
		
	public Coordinates getCoordinates(){
		return coordinates;
	}

	public Double getRadius() {
		return radius;
	}
	
}
