package org.boblycat.abbots.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.boblycat.abbots.Board;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class PuzzleServer {
    private static class Args {
        @Parameter(names = { "--www-root" }, description = "Static ww root directory")
        String wwwRoot = "www";
    }

    private static Board loadBoardFromResource(String name) {
        String path = "/" + name + ".board";
        try (InputStream stream = PuzzleServer.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new RuntimeException("Missing resource " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                Board board = new Board();
                board.parse(reader);
                return board;
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void generatePuzzleBoards(String name, int maxDepth, PuzzleSupplier puzzleSupplier,
            Collection<PuzzleCondition> conditions) {
        Board baseBoard = loadBoardFromResource(name);
        PuzzleGenerator generator = new PuzzleGenerator(baseBoard, false);
        AtomicInteger counter = new AtomicInteger();
        generator.generate(maxDepth, conditions).forEach((abbot, map) -> {
            int abbotIndex = counter.getAndIncrement();
            System.out.println("Abbot '" + abbot + "': " + map.size() + " results");
            if (!map.isEmpty()) {
                int longest = map.values().stream().mapToInt(x -> x.length).max().getAsInt();
                List<PuzzleSolution> longestResults = map.values().stream().filter(x -> x.length == longest)
                        .collect(Collectors.toList());
                System.out.println(" " + longestResults.size() + " unique puzzles with length " + longest);
                for (PuzzleSolution s: longestResults) {
                    /*
                    for (List<Move> moves: s.enumerateMoves()) {
                        System.out.println(
                                " " + moves.stream().map(m -> m.abbot + "|" + m.dir).collect(Collectors.joining(", ")));
                    }
                    */
                    //System.out.println(generator.boardWithTarget(abbot, s.key.abbotPositions[abbotIndex]));
                    //System.out.println(s.movesToString(" "));
                    Board board = generator.boardWithTarget(abbot, s.key.abbotPositions[abbotIndex]);
                    puzzleSupplier.add(board, s.movesToString(""));
                }
            }
        });
    }

    public static void main(String argv[]) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argv);
        Vertx vertx = Vertx.vertx();
        StaticHandler staticHandler = StaticHandler.create(args.wwwRoot);
        Router router = Router.router(vertx);
        PuzzleSupplier puzzleSupplier = new PuzzleSupplier();
        //generatePuzzleBoards("example-two-bots", 70, puzzleSupplier);
        generatePuzzleBoards("example-four-bots", 10, puzzleSupplier, List.of(
                //PuzzleCondition.unique(),
                PuzzleCondition.notAtEdge(), PuzzleCondition.inCorner(), PuzzleCondition.differentBotsMovedAtLeast(3)));
        router.get("/api/puzzles").handler(ctx -> {
            System.out.println("SIZE " + Integer.toString(puzzleSupplier.size()));
            ctx.response().setStatusCode(200).putHeader("context-type", "text/plain")
                    .end(Integer.toString(puzzleSupplier.size()));
        });
        router.get("/api/puzzles/:puzzleIndex/board").handler(ctx -> {
            int puzzleIndex = Integer.parseInt(ctx.request().getParam("puzzleIndex"));
            Board board = puzzleSupplier.boardAt(puzzleIndex);
            ctx.response().setStatusCode(200).putHeader("content-type", "text/plain").end(board.toString());
        });
        router.get("/api/puzzles/:puzzleIndex/solution").handler(ctx -> {
            int puzzleIndex = Integer.parseInt(ctx.request().getParam("puzzleIndex"));
            String solution = puzzleSupplier.solutionAt(puzzleIndex);
            ctx.response().setStatusCode(200).putHeader("content-type", "text/plain").end(solution);
        });
        router.route("/*").handler(staticHandler);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
