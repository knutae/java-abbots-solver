package org.boblycat.abbots.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Set;

import org.boblycat.abbots.Board;
import org.boblycat.abbots.Position;
import org.junit.Test;

public class TestGenerator {
    private static Position pos(int x, int y) {
        return Position.get(x, y);
    }

    @Test
    public void oneBotDepth2NoConditions() {
        List<PuzzleCondition> conditions = List.of();
        Board board = PuzzleServer.loadBoardFromResource("example-one-bot");
        PuzzleGenerator generator = new PuzzleGenerator(board, true);
        assertEquals(pos(12, 14), board.getAbbots().get('b'));
        generator.generate(2, conditions).forEach((abbot, map) -> {
            int longest = map.values().stream().mapToInt(x -> x.length).max().getAsInt();
            assertEquals(2, longest);
            map.values().removeIf(x -> x.length != longest);
            assertEquals(6, map.size());
            assertEquals(Set.of(pos(4, 9), pos(5, 15), pos(12, 14), pos(13, 15), pos(15, 10), pos(15, 15)),
                    map.keySet());
        });
    }

    @Test
    public void oneBotDepth2NoBotAtTarget() {
        List<PuzzleCondition> conditions = List.of(PuzzleCondition.noBotAtTarget());
        Board board = PuzzleServer.loadBoardFromResource("example-one-bot");
        PuzzleGenerator generator = new PuzzleGenerator(board, true);
        assertEquals(pos(12, 14), board.getAbbots().get('b'));
        generator.generate(2, conditions).forEach((abbot, map) -> {
            assertEquals(pos(12, 14), board.getAbbots().get('b'));
            int longest = map.values().stream().mapToInt(x -> x.length).max().getAsInt();
            assertEquals(2, longest);
            map.values().removeIf(x -> x.length != longest);
            assertFalse(map.keySet().contains(pos(12, 14)));
            assertEquals(5, map.size());
            assertEquals(Set.of(pos(4, 9), pos(5, 15), pos(13, 15), pos(15, 10), pos(15, 15)), map.keySet());
        });
    }
}
