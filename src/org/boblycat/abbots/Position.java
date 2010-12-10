package org.boblycat.abbots;

public class Position {
    public int x;
    public int y;
    
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Position other) {
        return other.x == x && other.y == y;
    }
    
    public boolean equals(Object other) {
        if (other instanceof Position)
            return equals((Position) other);
        return false;
    }
}
