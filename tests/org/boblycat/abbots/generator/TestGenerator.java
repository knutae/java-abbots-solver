package org.boblycat.abbots.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.boblycat.abbots.Board;
import org.boblycat.abbots.Position;
import org.junit.Test;

public class TestGenerator {
    private static final String ONE_BOT_BOARD = String.join("\n", //
            "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+", //
            "|       |               |       |", //
            "+ + + + + + + + + + + + + +-+ + +", //
            "|                         |     |", //
            "+ + + + + + + + + + + + + + + + +", //
            "|           |       |           |", //
            "+ + + + + +-+ + + +-+ + + + + + +", //
            "|                               |", //
            "+ + +-+ + + + + + + + + + + + +-+", //
            "|     |                         |", //
            "+-+ + + + + + + + + + + + + + + +", //
            "|             |             |   |", //
            "+ +-+ + + + + +-+ + + +-+ + +-+ +", //
            "| |                     |       |", //
            "+ + + + + + + +-+-+ + + + + + + +", //
            "|             |   |             |", //
            "+ + + + + + + + + + + + + + + + +", //
            "|             |   |             |", //
            "+ + + + + + + +-+-+ + + +-+ + + +", //
            "|       |                 |     |", //
            "+ + + + +-+ +-+ + + + + + + + +-+", //
            "|           |                   |", //
            "+-+ + + + + + + + + + + + + + + +", //
            "|                               |", //
            "+ + + + + + + +-+ +-+ + + + + + +", //
            "|               | |             |", //
            "+ +-+ + + + + + + + + + + + + + +", //
            "|   |                       |   |", //
            "+ + + + + + + + + + + + + + +-+ +", //
            "|       |               |b      |", //
            "+ + + +-+ + + + + + + +-+ + + + +", //
            "|         |                 |   |", //
            "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");

    private static Position pos(int x, int y) {
        return Position.get(x, y);
    }

    private static Board boardFromString(String input) {
        Board board = new Board();
        try {
            board.parse(input);
            return board;
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException");
            return null;
        }
    }

    @Test
    public void oneBotDepth2NoConditions() {
        List<PuzzleCondition> conditions = List.of();
        Board board = boardFromString(ONE_BOT_BOARD);
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
        Board board = boardFromString(ONE_BOT_BOARD);
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
