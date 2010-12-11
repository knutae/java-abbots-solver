package org.boblycat.abbots;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Board {
    public enum Direction {
        Up,
        Down,
        Left,
        Right,
    }
    
    private int width, height;
    private boolean[][] horizontalWalls;
    private boolean[][] verticalWalls;
    private HashMap<Character, Position> abbots;
    private HashMap<Character, Position> targets;
    
    public void parse(BufferedReader input) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        String line = input.readLine();
        while (line != null) {
            if (line.length() > 0)
                lines.add(line);
            line = input.readLine();
        }
        if (lines.size() < 3)
            throw new RuntimeException("Too few lines");
        height = (lines.size() - 1) / 2;
        width = lines.get(0).replaceAll("[+]", "").length();
        
        // process all vertical walls
        verticalWalls = new boolean[height][width+1];
        for (int y = 0; y < height; y++) {
            line = lines.get(y*2 + 1);
            //System.out.println(line);
            for (int x = 0; x <= width; x++) {
                char c = line.charAt(x*2);
                if (c == ' ')
                    verticalWalls[y][x] = false;
                else if (c == '|')
                    verticalWalls[y][x] = true;
                else
                    throw new RuntimeException("Expected ' ' or '|', got '" + c + "'");
            }
        }
        
        // process all horizontal walls
        horizontalWalls = new boolean[width][height+1];
        for (int y = 0; y <= height; y++) {
            line = lines.get(y*2);
            //System.out.println(line);
            for (int x = 0; x < width; x++) {
                char c = line.charAt(x*2 + 1);
                if (c == ' ')
                    horizontalWalls[x][y] = false;
                else if (c == '-')
                    horizontalWalls[x][y] = true;
                else
                    throw new RuntimeException("Expected ' ' or '-', got '" + c + "'");
            }
        }
        
        // process abbots and targets
        abbots = new HashMap<Character, Position>();
        targets = new HashMap<Character, Position>();
        for (int y = 0; y < height; y++) {
            line = lines.get(y*2 + 1);
            //System.out.println(line);
            for (int x = 0; x < width; x++) {
                char c = line.charAt(x*2 + 1);
                if (Character.isLowerCase(c))
                    abbots.put(c, new Position(x, y));
                else if (Character.isUpperCase(c))
                    targets.put(Character.toLowerCase(c), new Position(x, y));
                else if (c != ' ')
                    throw new RuntimeException("Expected letter or space, got '" + c + "'");
            }
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // create reverse lookup table for abbots and targets
        char[][] abbotChars = new char[width][height];
        for (int x = 0; x < width; x++)
            Arrays.fill(abbotChars[x], ' ');
        for (Entry<Character, Position> entry: targets.entrySet())  {
            abbotChars[entry.getValue().x][entry.getValue().y] = Character.toUpperCase(entry.getKey());
        }
        for (Entry<Character, Position> entry: abbots.entrySet())  {
            abbotChars[entry.getValue().x][entry.getValue().y] = entry.getKey();
        }
        // output lines
        for (int y = 0; y <= height; y++) {
            // horizontal line
            sb.append('+');
            for (int x = 0; x < width; x++) {
                if (horizontalWalls[x][y])
                    sb.append('-');
                else
                    sb.append(' ');
                sb.append('+');
            }
            sb.append('\n');
            if (y == height)
                break;
            // vertical line, and abbots
            for (int x = 0; x <= width; x++) {
                if (verticalWalls[y][x])
                    sb.append('|');
                else
                    sb.append(' ');
                if (x < width)
                    sb.append(abbotChars[x][y]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
    public boolean move(char abbot, Direction direction) {
        Position pos = abbots.get(abbot);
        boolean[] otherAbbots = null;
        boolean[] walls = null;
        int i = 0;
        // extract walls, abbots and index in the correct axis
        switch (direction) {
        case Up:
        case Down:
            otherAbbots = abbotsAtCol(pos.x);
            walls = horizontalWalls[pos.x];
            i = pos.y;
            break;
        case Left:
        case Right:
            otherAbbots = abbotsAtRow(pos.y);
            walls = verticalWalls[pos.y];
            i = pos.x;
            break;
        }
        // move index
        switch (direction) {
        case Up:
        case Left:
            while (walls[i] == false && otherAbbots[i-1] == false)
                i--;
            break;
        case Down:
        case Right:
            while (walls[i+1] == false && otherAbbots[i+1] == false)
                i++;
            break;
        }
        // update position and detect if the abbot was actually moved
        switch (direction) {
        case Up:
        case Down:
            if (pos.y == i)
                return false;
            pos.y = i;
            break;
        case Left:
        case Right:
            if (pos.x == i)
                return false;
            pos.x = i;
            break;
        }
        return true;
    }
    
    public boolean isSolved() {
        for (Entry<Character, Position> target: targets.entrySet()) {
            if (!abbots.get(target.getKey()).equals(target.getValue()))
                return false;
        }
        return true;
    }
    
    public Map<Character, Position> getAbbots() {
        return abbots;
    }
    
    private boolean[] abbotsAtRow(int y) {
        boolean[] line = new boolean[width]; // false initially
        for (Position pos: abbots.values()) {
            if (pos.y == y)
                line[pos.x] = true;
        }
        return line;
    }
    
    private boolean[] abbotsAtCol(int x) {
        boolean[] line = new boolean[height]; // false initially
        for (Position pos: abbots.values()) {
            if (pos.x == x)
                line[pos.y] = true;
        }
        return line;
    }
    
    public static void main(String[] args) throws IOException {
        Board b = new Board();
        if (args.length == 0)
            b.parse(new BufferedReader(new InputStreamReader(System.in)));
        else
            b.parse(new BufferedReader(new FileReader(args[0])));
        System.out.println(b.toString());
        System.out.println("Solved: " + b.isSolved());
        b.move('r', Direction.Left);
        b.move('r', Direction.Right);
        b.move('r', Direction.Up);
        //b.move('r', Direction.Up);
        //b.move('r', Direction.Right);
        System.out.println(b.toString());
        System.out.println("Solved: " + b.isSolved());
    }
}
