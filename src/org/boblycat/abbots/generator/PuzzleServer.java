package org.boblycat.abbots.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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

    public static void main(String argv[]) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argv);
        Vertx vertx = Vertx.vertx();
        StaticHandler staticHandler = StaticHandler.create(args.wwwRoot);
        Router router = Router.router(vertx);
        router.get("/puzzle").handler(req -> {
            Board board = loadBoardFromResource("example-one-bot");
            req.response().setStatusCode(200).putHeader("content-type", "text/plain").end(board.toString());
        });
        router.route("/*").handler(staticHandler);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
