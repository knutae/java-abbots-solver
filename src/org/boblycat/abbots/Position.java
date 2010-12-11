package org.boblycat.abbots;

public class Position {
    public final int x;
    public final int y;
    
    private static Position[][] positionTable;
    
    public static void init(int width, int height) {
        positionTable = new Position[width][height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                positionTable[x][y] = new Position(x, y);
    }
    
    public static Position get(int x, int y) {
        return positionTable[x][y];
    }
    
    private Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
