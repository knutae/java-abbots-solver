package org.boblycat.abbots.generator;

import java.util.ArrayList;
import java.util.List;

import org.boblycat.abbots.Board;

public class PuzzleSupplier {
    private static class PuzzleEntry {
        private final Board board;
        private final String solution;

        PuzzleEntry(Board board, String solution) {
            this.board = board;
            this.solution = solution;
        }
    }

    private final List<PuzzleEntry> boards = new ArrayList<>();

    public void add(Board board, String solution) {
        boards.add(new PuzzleEntry(board, solution));
    }

    public Board boardAt(int index) {
        return boards.get(index).board;
    }

    public String solutionAt(int index) {
        return boards.get(index).solution;
    }

    int size() {
        return boards.size();
    }
}
