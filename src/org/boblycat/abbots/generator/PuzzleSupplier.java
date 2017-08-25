package org.boblycat.abbots.generator;

import java.util.List;

import org.boblycat.abbots.Board;

public class PuzzleSupplier {
    private final List<Board> boards;
    private int nextIndex = 0;

    public PuzzleSupplier(List<Board> boards) {
        this.boards = boards;
    }

    public Board nextBoard() {
        Board next = boards.get(nextIndex);
        nextIndex = (nextIndex + 1) % boards.size();
        return next;
    }
}
