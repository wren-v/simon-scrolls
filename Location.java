import java.awt.Color;

public class Location {
    
    private int row;
    private int col;
    
    public Location(int r, int c) {
        row = r;
        col = c;
    }
    
    
    public int getRow() {
        return row;
    }
    
    public void setRow(int amount) {
    	row = amount;
    }
    
    public int getCol() {
        return col;
    }
    
    public void setCol(int amount) {
    	col = amount;
    }
    
    public boolean equals(Location otherLoc) {
        return row == otherLoc.getRow() && col == otherLoc.getCol();
    }
    
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
    
}