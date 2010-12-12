package org.boblycat.abbots;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestSolver {
    private static final String SMALL_BOARD =
        "+-+-+-+\n" +
        "|r    |\n" +
        "+-+ +-+\n" +
        "|R b  |\n" +
        "+-+-+-+";
    
    private static final String ONE_MOVE_BOARD =
        "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n" +
        "|       |               |       |\n" +
        "+ + + + + + + + + + + + + +-+ + +\n" +
        "|                         |R    |\n" +
        "+ + + + + + + + + + + + + + + + +\n" +
        "|           |       |           |\n" +
        "+ + + + + +-+ + + +-+ + + + + + +\n" +
        "|                               |\n" +
        "+ + +-+ + + + + + + + + + + + +-+\n" +
        "|     |                         |\n" +
        "+-+ + + + + + + + + + + + + + + +\n" +
        "|             |             |   |\n" +
        "+ +-+ + + + + +-+ + + +-+ + +-+ +\n" +
        "| |y                    |       |\n" +
        "+ + + + + + + +-+-+ + + + + + + +\n" +
        "|             |   |             |\n" +
        "+ + + + + + + + + + + + + + + + +\n" +
        "|             |   |             |\n" +
        "+ + + + + + + +-+-+ + + +-+ + + +\n" +
        "|       |                b|     |\n" +
        "+ + + + +-+ +-+ + + + + + + + +-+\n" +
        "|           |                   |\n" +
        "+-+ + + + + + + + + + + + + + + +\n" +
        "|                               |\n" +
        "+ + + + + + + +-+ +-+ + + + + + +\n" +
        "|               | |             |\n" +
        "+ +-+ + + + + + + + + + + + + + +\n" +
        "|   |                       |   |\n" +
        "+ + + + + + + + + + + + + + +-+ +\n" +
        "|      g|               |       |\n" +
        "+ + + +-+ + + + + + + +-+ + + + +\n" +
        "|         |                r|   |\n" +
        "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+";

    private static final String THREE_MOVE_BOARD =
        "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n" +
        "|       |               |       |\n" +
        "+ + + + + + + + + + + + + +-+ + +\n" +
        "|                         |R    |\n" +
        "+ + + + + + + + + + + + + + + + +\n" +
        "|           |      y|           |\n" +
        "+ + + + + +-+ + + +-+ + + + + + +\n" +
        "|                               |\n" +
        "+ + +-+ + + + + + + + + + + + +-+\n" +
        "|     |                         |\n" +
        "+-+ + + + + + + + + + + + + + + +\n" +
        "|             |             |   |\n" +
        "+ +-+ + + + + +-+ + + +-+ + +-+ +\n" +
        "| |                     |       |\n" +
        "+ + + + + + + +-+-+ + + + + + + +\n" +
        "|             |   |             |\n" +
        "+ + + + + + + + + + + + + + + + +\n" +
        "|             |   |             |\n" +
        "+ + + + + + + +-+-+ + + +-+ + + +\n" +
        "|       |                 |     |\n" +
        "+ + + + +-+ +-+ + + + + + + + +-+\n" +
        "|           |                   |\n" +
        "+-+ + + + + + + + + + + + + + + +\n" +
        "|                               |\n" +
        "+ + + + + + + +-+ +-+ + + + + + +\n" +
        "|               | |             |\n" +
        "+ +-+ + + + + + + + + + + + + + +\n" +
        "|   |                       |   |\n" +
        "+ + + + + + + + + + + + + + +-+ +\n" +
        "|       |    r         g|b      |\n" +
        "+ + + +-+ + + + + + + +-+ + + + +\n" +
        "|         |                 |   |\n" +
        "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+";

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[] {SMALL_BOARD, 5, "b^b>r>r,r<"});
        list.add(new Object[] {ONE_MOVE_BOARD, 1, "r^"});
        list.add(new Object[] {THREE_MOVE_BOARD, 3, "r,r>r^"});
        return list;
    }
    
    private Board board;
    private Solver solver;
    private int expectedDepth;
    private String expectedSolution;
    
    public TestSolver(String boardData, int expectedDepth, String expectedSolution) throws IOException {
        board = new Board();
        board.parse(boardData);
        solver = new Solver(board);
        this.expectedDepth = expectedDepth;
        this.expectedSolution = expectedSolution;
    }
    
    @Test
    public void test() {
        String solution = solver.solve("");
        assertEquals(expectedDepth * 2, solution.length());
        if (expectedSolution != null)
            assertEquals(expectedSolution, solution);
    }
}
