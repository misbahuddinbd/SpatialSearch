package protocols;

import peersim.core.Protocol;

public class localMBR implements Protocol  {

    private double lx, ly, hx, hy;

    public localMBR(String prefix) {
        lx = ly = hx = hy = -1;
    }

    public double getLX() {
        return lx;
    }

    public void setLX(double x) {
        this.lx = x;
    }

    public double getLY() {
        return ly;
    }

    public void setLY(double y) {
        this.ly = y;
    }

    public double getHX() {
        return hx;
    }

    public void setHX(double x) {
        this.hx = x;
    }

    public double getHY() {
        return hy;
    }

    public void setHY(double y) {
        this.hy = y;
    }

    @Override
    public String toString(){
    	
		return this.lx+" "+this.ly+" "+this.hx+" "+this.hy;
    	
    }
    
    public Object clone() {
        localMBR inp = null;
        try {
            inp = (localMBR) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return inp;
    }
    
}
