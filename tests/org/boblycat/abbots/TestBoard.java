package org.boblycat.abbots;

import static org.junit.Assert.*;

import java.io.IOException;

import org.boblycat.abbots.Board.Direction;
import org.junit.Before;
import org.junit.Test;

public class TestBoard {
    private Board board;
    
    private static String SMALL_BOARD =
        "+-+-+-+\n" +
        "|r    |\n" +
        "+-+ +-+\n" +
        "|R b  |\n" +
        "+-+-+-+";
    
    @Before
    public void setUp() {
        board = new Board();
    }
    
    private static void checkPosition(Position pos, int x, int y) {
        assertNotNull(pos);
        assertEquals(x, pos.x);
        assertEquals(y, pos.y);
    }
    
    private void checkAbbotPosition(char abbot, int x, int y) {
        checkPosition(board.getAbbots().get(abbot), x, y);
    }
    
    @Test
    public void parse() throws IOException {
        board.parse(SMALL_BOARD);
        assertEquals(3, board.getWidth());
        assertEquals(2, board.getHeight());
        assertEquals(2, board.getAbbots().size());
        checkAbbotPosition('r', 0, 0);
        checkAbbotPosition('b', 1, 1);
        assertEquals(1, board.getTargets().size());
        checkPosition(board.getTargets().get('r'), 0, 1);
    }
    
    @Test
    public void move() throws IOException {
        board.parse(SMALL_BOARD);
        assertFalse(board.move('r', Direction.Up));
        assertFalse(board.move('r', Direction.Down));
        assertFalse(board.move('r', Direction.Left));
        assertTrue(board.move('r', Direction.Right));
        checkAbbotPosition('r', 2, 0);
        assertFalse(board.move('b', Direction.Down));
        assertTrue(board.move('b', Direction.Up));
        checkAbbotPosition('b', 1, 0);
        assertFalse(board.move('b', Direction.Right));
        assertTrue(board.move('b', Direction.Left));
        checkAbbotPosition('b', 0, 0);
        assertTrue(board.move('r', Direction.Left));
        checkAbbotPosition('r', 1, 0);
        assertTrue(board.move('r', Direction.Down));
        checkAbbotPosition('r', 1, 1);
        assertTrue(board.move('r', Direction.Right));
        checkAbbotPosition('r', 2, 1);
        assertTrue(board.move('r', Direction.Left));
        checkAbbotPosition('r', 0, 1);
    }
    
    @Test
    public void isSolved() throws IOException {
        board.parse(SMALL_BOARD);
        assertFalse(board.isSolved());
        assertTrue(board.move('b', Direction.Up));
        assertFalse(board.isSolved());
        assertTrue(board.move('b', Direction.Right));
        assertFalse(board.isSolved());
        assertTrue(board.move('r', Direction.Right));
        assertFalse(board.isSolved());
        assertTrue(board.move('r', Direction.Down));
        assertFalse(board.isSolved());
        assertTrue(board.move('r', Direction.Left));
        assertTrue(board.isSolved());
    }
}
