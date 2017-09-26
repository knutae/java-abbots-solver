package org.boblycat.abbots.generator;

import org.boblycat.abbots.Board;
import org.boblycat.abbots.Position;

@FunctionalInterface
public interface PuzzleCondition {
    boolean includeSolution(Board board, Position position, PuzzleSolution solution);

    static PuzzleCondition unique() {
        return (b, p, s) -> s.isUnique();
    }

    static PuzzleCondition lengthAtLeast(int length) {
        return (b, p, s) -> s.length >= length;
    }

    static PuzzleCondition inCorner() {
        return (b, p, s) -> (b.hasHorizontalWall(p.x, p.y) || b.hasHorizontalWall(p.x, p.y + 1))
                && (b.hasVerticalWall(p.x, p.y) || b.hasVerticalWall(p.x + 1, p.y));
    }

    static PuzzleCondition notAtEdge() {
        return (b, p, s) -> p.x > 0 && p.x < b.getWidth() - 1 && p.y > 0 && p.y < b.getHeight() - 1;
    }

    static PuzzleCondition differentBotsMovedAtLeast(int n) {
        return (b, p, s) -> s.minimumDifferentBotsMoved() >= n;
    }

    static PuzzleCondition noBotAtTarget() {
        return (b, p, s) -> {
            //System.err.println(p + " vs " + b.getAbbots().values() + " --> " + !b.getAbbots().values().contains(p));
            return !b.getAbbots().values().contains(p);
        };
    }
}
