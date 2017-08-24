package org.boblycat.abbots.generator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class PuzzleServer {
    private static class Args {
        @Parameter(names = { "--www-root" }, description = "Static ww root directory")
        String wwwRoot = "../../wobots";
    }

    public static void main(String argv[]) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(argv);
        Vertx vertx = Vertx.vertx();
        StaticHandler staticHandler = StaticHandler.create(args.wwwRoot);
        Router router = Router.router(vertx);
        router.route("/*").handler(staticHandler);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
