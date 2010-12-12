package org.boblycat.abbots;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
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

class SearchKey {
    final Position[] abbotPos;
    final private Position[] sortedAbbotPos;
    final private int cachedHashCode;
    final private int targetIndex;
    
    public SearchKey(SortedMap<Character, Position> abbots, int targetIndex) {
        abbotPos = new Position[abbots.size()];
        int hc = 0;
        int i = 0;
        for (Position pos: abbots.values()) {
            if (i == targetIndex)
                hc += pos.hashCode() << 1;
            else
                hc += pos.hashCode();
            abbotPos[i] = pos;
            i++;
        }
        cachedHashCode = hc;
        this.targetIndex = targetIndex;

        sortedAbbotPos = new Position[abbotPos.length-1];
        for (i = 0; i < abbotPos.length; i++) {
            if (i < targetIndex)
                sortedAbbotPos[i] = abbotPos[i];
            else if (i > targetIndex)
                sortedAbbotPos[i-1] = abbotPos[i];
        }
        Arrays.sort(sortedAbbotPos);
    }
    
    public int hashCode() {
        return cachedHashCode;
    }
    
    public boolean equals(SearchKey other) {
        if (abbotPos[targetIndex] != other.abbotPos[targetIndex])
            return false;
        for (int i = 0; i < sortedAbbotPos.length; i++) {
            if (sortedAbbotPos[i] != other.sortedAbbotPos[i])
                return false;
        }
        return true;
    }
    
    public boolean equals(Object other) {
        if (other instanceof SearchKey)
            return equals((SearchKey) other);
        return false;
    }
}

class SearchNode {
    SearchKey key;
    Move[] moves;
    
    public SearchNode(SearchKey key, SearchNode parent, Move thisMove) {
        this.key = key;
        if (parent == null)
            moves = new Move[0];
        else {
            Move[] prevMoves = parent.moves;
            moves = new Move[prevMoves.length + 1];
            for (int i = 0; i < prevMoves.length; i++)
                moves[i] = prevMoves[i];
            moves[prevMoves.length] = thisMove;
        }
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
    private HashMap<SearchKey, SearchNode> searchMap;
    private Position targetPosition;
    private int targetIndex;
    private boolean verbose;
    
    public Solver(Board board, boolean verbose) {
        this.board = board;
        this.verbose = verbose;
        searchMap = new HashMap<SearchKey, SearchNode>();
        moves = new Move[board.getAbbots().size() * Direction.values().length];
        int i = 0;
        for (char abbot: board.getAbbots().keySet()) {
            for (Direction dir: Direction.values())
                moves[i++] = new Move(abbot, dir);
        }
        assert (i == moves.length);
        
        SortedMap<Character, Position> targets = board.getTargets();
        if (targets.size() != 1)
            throw new RuntimeException("Need exactly one target");
        targetPosition = targets.values().iterator().next();
        // index of targetEntry will be the same for all nodes, since they are sorted by abbot
        targetIndex = board.abbotIndex(targets.keySet().iterator().next());
        root = new SearchNode(new SearchKey(board.getAbbots(), targetIndex), null, null);
    }
    
    private void resetBoardAbbots(SearchNode node) {
        int i = 0;
        for (Entry<Character, Position> entry: board.getAbbots().entrySet()) {
            Position pos = node.key.abbotPos[i];
            entry.setValue(pos);
            i++;
        }
    }
    
    public String solve(String movesSep) {
        List<SearchNode> currentNodes = new ArrayList<SearchNode>();
        currentNodes.add(root);
        int depth = 1;
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
                    SearchKey newKey = new SearchKey(board.getAbbots(), targetIndex);
                    if (searchMap.containsKey(newKey))
                        continue;
                    SearchNode subNode = new SearchNode(newKey, node, move);
                    searchMap.put(newKey, subNode);
                    
                    // found a new node, process it
                    if (subNode.key.abbotPos[targetIndex].equals(targetPosition)) {
                        assert (board.isSolved());
                        if (verbose)
                            System.out.println("Found solution with depth " + depth);
                        return subNode.movesToString(movesSep);
                    }
                    nextNodes.add(subNode);
                }
            }
            if (verbose)
                System.out.println("Depth " + depth + ", map size " + searchMap.size());
            depth++;
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
        Solver solver = new Solver(b, true);
        String solution = solver.solve(" ");
        long endTime = System.currentTimeMillis();
        System.out.println("Solution: " + solution);
        //System.out.println(b.toString());
        System.out.println("Duration: " + (endTime - startTime) + " ms");
    }
}
