package org.boblycat.abbots.generator;

import java.util.List;

import org.boblycat.abbots.Board;

public class PuzzleSupplier {
    private final List<Board> boards;

    public PuzzleSupplier(List<Board> boards) {
        this.boards = boards;
    }

    public Board boardAt(int index) {
        return boards.get(index % boards.size());
    }

    int size() {
        return boards.size();
    }
}
