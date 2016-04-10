package protocols;

import peersim.core.Protocol;

public class Coordinates implements Protocol  {

    private double x, y;

    public Coordinates(String prefix) {
        x = y = -1;
    }
    
    public Coordinates() {
        x = y = -1;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString(){
    	
		return this.x+" "+this.y;
    	
    }
    public Object clone() {
        Coordinates inp = null;
        try {
            inp = (Coordinates) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
    
}
