package org.boblycat.abbots;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.boblycat.abbots.Board.Direction;

class Move {
    char abbot;
    Board.Direction dir;
    
    Move(char abbot, Board.Direction dir) {
        this.abbot = abbot;
        this.dir = dir;
    }
}

class SearchEntry {
    char abbot;
    int x;
    int y;
    
    public SearchEntry(Entry<Character, Position> mapEntry) {
        abbot = mapEntry.getKey();
        Position pos = mapEntry.getValue();
        x = pos.x;
        y = pos.y;
    }
    
    public boolean equals(SearchEntry other) {
        return abbot == other.abbot && x == other.x && y == other.y;
    }
    
    public boolean equals(Object other) {
        if (other instanceof SearchEntry)
            return equals((SearchEntry) other);
        return false;
    }
    
    public int hashCode() {
        int a = abbot;
        return x ^ (y << 8) ^ (a << 16); 
    }
}

class SearchNode {
    Set<SearchEntry> keySet;
    List<Move> moves;
    
    public SearchNode(Map<Character, Position> abbots, List<Move> moves) {
       keySet = new HashSet<SearchEntry>();
       for (Entry<Character, Position> entry: abbots.entrySet())
           keySet.add(new SearchEntry(entry));
       this.moves = moves;
    }
    
    public String movesToString(String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Move m: moves) {
            if (first)
                first = false;
            else
                sb.append(sep);
            sb.append(m.abbot);
            switch (m.dir) {
            case Up:
                sb.append('^');
                break;
            case Down:
                sb.append(',');
                break;
            case Left:
                sb.append('<');
                break;
            case Right:
                sb.append('>');
                break;
            }
        }
        return sb.toString();
    }
}

public class Solver {
    private Board board;
    private Move[] moves;
    private SearchNode root;
    private HashMap<Set<SearchEntry>, SearchNode> searchMap;
    
    public Solver(Board board) {
        this.board = board;
        searchMap = new HashMap<Set<SearchEntry>, SearchNode>();
        root = new SearchNode(board.getAbbots(), new ArrayList<Move>(0));
        moves = new Move[board.getAbbots().size() * Direction.values().length];
        int i = 0;
        for (char abbot: board.getAbbots().keySet()) {
            for (Direction dir: Direction.values())
                moves[i++] = new Move(abbot, dir);
        }
        assert (i == moves.length);
    }
    
    private void resetBoardAbbots(SearchNode node) {
        Map<Character, Position> abbotMap = board.getAbbots();
        for (SearchEntry entry: node.keySet) {
            Position pos = abbotMap.get(entry.abbot);
            pos.x = entry.x;
            pos.y = entry.y;
        }
    }
    
    String solve(String movesSep) {
        List<SearchNode> currentNodes = new ArrayList<SearchNode>();
        currentNodes.add(root);
        int i = 0;
        while (true) {
            List<SearchNode> nextNodes = new ArrayList<SearchNode>();
            for (SearchNode node: currentNodes) {
                boolean needsReset = true;
                for (Move move: moves) {
                    if (needsReset)
                        resetBoardAbbots(node);
                    if (!board.move(move.abbot, move.dir)) {
                        // not moved
                        needsReset = false;
                        continue;
                    }
                    needsReset = true;
                    ArrayList<Move> newMoves = new ArrayList<Move>(node.moves.size()+1);
                    newMoves.addAll(node.moves);
                    newMoves.add(move);
                    SearchNode subNode = new SearchNode(board.getAbbots(), newMoves);
                    if (searchMap.containsKey(subNode.keySet))
                        continue;
                    searchMap.put(subNode.keySet, subNode);
                    
                    // found a new node, process it
                    if (board.isSolved())
                        return subNode.movesToString(movesSep);
                    nextNodes.add(subNode);
                }
            }
            i++;
            System.out.println("Depth " + i + ", map size " + searchMap.size());
            currentNodes = nextNodes;
        }
    }
    
    public static void main(String[] args) throws IOException {
        Board b = new Board();
        if (args.length == 0)
            b.parse(new BufferedReader(new InputStreamReader(System.in)));
        else
            b.parse(new BufferedReader(new FileReader(args[0])));
        System.out.println(b.toString());
        long startTime = System.currentTimeMillis();
        Solver solver = new Solver(b);
        String solution = solver.solve(" ");
        long endTime = System.currentTimeMillis();
        System.out.println("Solution: " + solution);
        System.out.println(b.toString());
        System.out.println("Duration: " + (endTime - startTime) + " ms");
    }
}
