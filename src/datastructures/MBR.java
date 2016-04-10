package datastructures;

import protocols.Coordinates;

public class MBR {
	
	private Double minX;
	private Double minY;
	private Double maxX;
	private Double maxY;
	
	public MBR() {
		this.minX = 0.0;
		this.minY = 0.0;
		this.maxX = 0.0;
		this.maxY = 0.0;
	}	
	
	public MBR(String prefix) {
		this.minX = 0.0;
		this.minY = 0.0;
		this.maxX = 0.0;
		this.maxY = 0.0;
	}	
	
	public MBR(Coordinates coordinates) {
		this.minX = coordinates.getX();
		this.minY = coordinates.getY();
		this.maxX = coordinates.getX();
		this.maxY = coordinates.getY();
	}
	
	public MBR(MBR mbr) {
		this.minX = mbr.getMinX();
		this.minY = mbr.getMinY();
		this.maxX = mbr.getMaxX();
		this.maxY = mbr.getMaxY();
	}	
	
	public MBR(Double minX, Double minY, Double maxX, Double maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}	
	
	public MBR(Disc disc){
		
		this.minX = disc.getCoordinates().getX() - disc.getRadius();
		this.minY = disc.getCoordinates().getY() - disc.getRadius();
		this.maxX = disc.getCoordinates().getX() + disc.getRadius();
		this.maxY = disc.getCoordinates().getY() + disc.getRadius();
		
		if (this.minX < 0) this.minX = 0.0;
		if (this.minY < 0) this.minY = 0.0;
		
	}
	
	public Double getMinX() {
		return minX;
	}

	public void setMinX(Double minX) {
		this.minX = minX;
	}

	public Double getMinY() {
		return minY;
	}

	public void setMinY(Double minY) {
		this.minY = minY;
	}

	public Double getMaxX() {
		return maxX;
	}

	public void setMaxX(Double maxX) {
		this.maxX = maxX;
	}

	public Double getMaxY() {
		return maxY;
	}

	public void setMaxY(Double maxY) {
		this.maxY = maxY;
	}
		
	@Override
	public String toString(){
		return "("+this.minX+","+this.minY+"),("+this.maxX+","+this.maxY+")";
	}

}
