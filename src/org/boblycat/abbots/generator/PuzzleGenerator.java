package org.boblycat.abbots.generator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.boblycat.abbots.Board;
import org.boblycat.abbots.Board.Direction;
import org.boblycat.abbots.Position;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

class Move {
    char abbot;
    Board.Direction dir;

    Move(char abbot, Board.Direction dir) {
        this.abbot = abbot;
        this.dir = dir;
    }
}

class PuzzleKey {
    final Position[] abbotPositions;
    final int cachedHashCode;

    public PuzzleKey(SortedMap<Character, Position> abbots) {
        abbotPositions = new Position[abbots.size()];
        int i = 0;
        int hc = 0;
        for (Position pos: abbots.values()) {
            abbotPositions[i] = pos;
            hc = (hc << 1) + pos.hashCode();
            i++;
        }
        cachedHashCode = hc;
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    public boolean equals(PuzzleKey other) {
        // since abbots are initialized from a sorted map which is the same for all keys, this is safe
        for (int i = 0; i < abbotPositions.length; i++) {
            if (abbotPositions[i] != other.abbotPositions[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PuzzleKey) {
            return equals((PuzzleKey) other);
        }
        return false;
    }
}

class PuzzleSolution {
    final PuzzleKey key;
    final Move move;
    final List<PuzzleSolution> parents;
    final int length;

    public PuzzleSolution(PuzzleKey key, PuzzleSolution parent, Move thisMove) {
        this.key = key;
        this.parents = new ArrayList<>();
        if (parent != null) {
            parents.add(parent);
        }
        this.move = thisMove;
        this.length = parent == null ? 0 : parent.length + 1;
    }

    public boolean isUnique() {
        return parents.isEmpty() || (parents.size() == 1 && parents.get(0).isUnique());
    }

    public String movesToString(String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        // Traverse parents to extract moves list
        List<Move> moves = new ArrayList<Move>();
        PuzzleSolution node = this;
        while (node != null && node.move != null) {
            moves.add(node.move);
            node = node.parents.isEmpty() ? null : node.parents.get(0);
        }
        Collections.reverse(moves);

        for (Move m: moves) {
            if (first) {
                first = false;
            } else {
                sb.append(sep);
            }
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

class PostProcessing {
    static Map<Position, List<PuzzleSolution>> findPuzzlesPerPosition(int abbotIndex,
            Collection<PuzzleSolution> solutions) {
        Map<Position, List<PuzzleSolution>> result = new HashMap<>();
        for (PuzzleSolution solution: solutions) {
            Position pos = solution.key.abbotPositions[abbotIndex];
            List<PuzzleSolution> list = result.computeIfAbsent(pos, _pos -> new ArrayList<>());
            if (list.isEmpty()) {
                // no previous solution for this position
                list.add(solution);
            } else {
                PuzzleSolution first = list.get(0);
                if (first.length == solution.length) {
                    // same length solution for this position
                    list.add(solution);
                } else if (first.length > solution.length) {
                    // shorter solution for this position
                    list.clear();
                    list.add(solution);
                }
            }
        }
        // Remove all non-unique solutions
        for (List<PuzzleSolution> solutionsForPos: result.values()) {
            solutionsForPos.removeIf(s -> !s.isUnique());
        }
        result.values().removeIf(solutionsForPos -> solutionsForPos.isEmpty());
        return result;
    }

    static Map<Position, PuzzleSolution> findUniquePuzzlesForAbbotPerPosition(int abbotIndex,
            Collection<PuzzleSolution> solutions) {
        return findPuzzlesPerPosition(abbotIndex, solutions).entrySet().stream().filter(e -> e.getValue().size() == 1)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get(0)));
    }

    static SortedMap<Character, Map<Position, PuzzleSolution>> findUniquePuzzlesPerAbbotAndPosition(
            Collection<Character> abbots, Collection<PuzzleSolution> solutions) {
        AtomicInteger abbotIndex = new AtomicInteger(0);
        SortedMap<Character, Map<Position, PuzzleSolution>> result = new TreeMap<>();
        abbots.stream().sorted().forEach(abbot -> {
            result.put(abbot, findUniquePuzzlesForAbbotPerPosition(abbotIndex.get(), solutions));
            abbotIndex.incrementAndGet();
        });
        return result;
    }
}

public class PuzzleGenerator {
    private final Board board;
    private final Move[] moves;
    private final boolean verbose;
    private final SortedMap<Character, Position> originalAbbots;

    public PuzzleGenerator(Board board, boolean verbose) {
        this.board = board;
        this.verbose = verbose;
        this.originalAbbots = Collections.unmodifiableSortedMap(new TreeMap<>(board.getAbbots()));
        moves = new Move[board.getAbbots().size() * Direction.values().length];
        int i = 0;
        for (char abbot: board.getAbbots().keySet()) {
            for (Direction dir: Direction.values()) {
                moves[i++] = new Move(abbot, dir);
            }
        }
        assert (i == moves.length);
    }

    static void resetBoardAbbots(Board board, PuzzleSolution node) {
        int i = 0;
        for (Entry<Character, Position> entry: board.getAbbots().entrySet()) {
            Position pos = node.key.abbotPositions[i];
            entry.setValue(pos);
            i++;
        }
    }

    public SortedMap<Character, Map<Position, PuzzleSolution>> generate(int maxDepth) {
        PuzzleSolution root = new PuzzleSolution(new PuzzleKey(originalAbbots), null, null);
        HashMap<PuzzleKey, PuzzleSolution> nodes = new HashMap<>();
        List<PuzzleSolution> currentNodes = new ArrayList<>();
        currentNodes.add(root);
        for (int depth = 1; depth <= maxDepth; depth++) {
            Map<PuzzleKey, PuzzleSolution> nextNodes = new HashMap<>();
            for (PuzzleSolution node: currentNodes) {
                boolean needsReset = true;
                for (Move move: moves) {
                    if (needsReset) {
                        resetBoardAbbots(board, node);
                    }
                    if (!board.move(move.abbot, move.dir)) {
                        // not moved
                        needsReset = false;
                        continue;
                    }
                    needsReset = true;
                    PuzzleKey newKey = new PuzzleKey(board.getAbbots());
                    PuzzleSolution existingSolution = nextNodes.get(newKey);
                    if (existingSolution != null) {
                        // found an existing at this depth
                        existingSolution.parents.add(node);
                        continue;
                    }
                    if (nodes.containsKey(newKey)) {
                        // a shorter path exists, ignore
                        continue;
                    }
                    PuzzleSolution subNode = new PuzzleSolution(newKey, node, move);
                    nodes.put(newKey, subNode);
                    nextNodes.put(subNode.key, subNode);
                }
            }
            if (verbose) {
                System.out.println("Depth " + depth + ", nodes " + nodes.size() + ", current " + nextNodes.size());
            }
            if (nextNodes.isEmpty()) {
                if (verbose) {
                    System.out.println("No solutions at depth " + depth);
                }
                break;
            }
            currentNodes.clear();
            currentNodes.addAll(nextNodes.values());
        }
        return PostProcessing.findUniquePuzzlesPerAbbotAndPosition(board.getAbbots().keySet(), nodes.values());
    }

    public Board boardWithTarget(char abbot, Position targetPosition) {
        return board.cloneWithAbbotsAndTargets(originalAbbots, Collections.singletonMap(abbot, targetPosition));
    }

    private static class Args {
        @Parameter(names = { "-f", "--filename" }, description = "Board filename")
        String filename = "-";

        @Parameter(names = { "-d", "--max-depth" }, description = "Max search depth")
        int maxDepth = 20;
    }

    public static void main(String[] argv) throws IOException, InterruptedException {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argv);
        Board b = new Board();
        if (args.filename.equals("-")) {
            b.parse(new BufferedReader(new InputStreamReader(System.in)));
        } else {
            b.parse(new BufferedReader(new FileReader(args.filename)));
        }
        System.out.println(b.toString());
        long startTime = System.currentTimeMillis();
        PuzzleGenerator generator = new PuzzleGenerator(b, true);
        //String solution = solver.solve(" ");
        //int cpus = Runtime.getRuntime().availableProcessors();
        //System.out.println("Using " + cpus + " threads");
        //String solution = solver.solveMultiThreaded(cpus, " ");
        //String solution = solver.solveMultiThreaded(4, " ");
        Map<Character, Map<Position, PuzzleSolution>> results = generator.generate(args.maxDepth);
        long endTime = System.currentTimeMillis();
        AtomicInteger counter = new AtomicInteger();
        results.forEach((abbot, r) -> {
            int abbotIndex = counter.getAndIncrement();
            System.out.println("Abbot '" + abbot + "': " + r.size() + " results");
            if (!r.isEmpty()) {
                int longest = r.values().stream().mapToInt(x -> x.length).max().getAsInt();
                List<PuzzleSolution> longestResults = r.values().stream().filter(x -> x.length == longest)
                        .collect(Collectors.toList());
                System.out.println(" " + longestResults.size() + " unique puzzles with length " + longest);
                for (PuzzleSolution s: longestResults) {
                    System.out.println(generator.boardWithTarget(abbot, s.key.abbotPositions[abbotIndex]));
                    System.out.println(s.movesToString(" "));
                }
            }
        });
        //System.out.println(b.toString());
        System.out.println("Duration: " + (endTime - startTime) + " ms");
    }
}
