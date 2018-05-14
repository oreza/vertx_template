package com.intuire.vertx;

import io.vertx.core.AbstractVerticle;

import java.time.Instant;
import java.util.*;

import io.vertx.core.json.*;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;
import xyz.jetdrone.vertx.hot.reload.HotReload;

import static java.time.temporal.ChronoUnit.DAYS;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {
    // your code goes here...

    final HandlebarsTemplateEngine engine = HandlebarsTemplateEngine.create();
    final Router router = Router.router(vertx);

    // development hot reload
    router.get().handler(HotReload.create());

    router.get("/").handler(ctx -> {
      // we define a hardcoded title for our application
      ctx.put("title", "Home Page");
      ctx.put("hotreload", System.getenv("VERTX_HOT_RELOAD"));

      engine.render(ctx, "templates", "/index.hbs", res -> {
        if (res.succeeded()) {
          ctx.response().end(res.result());
        } else {
          ctx.fail(res.cause());
        }
      });
    });

    // the example weather API
    List<String> SUMMARIES = Arrays.asList("Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching");

    router.get("/api/weather-forecasts").handler(ctx -> {
      final JsonArray response = new JsonArray();
      final Instant now = Instant.now();
      final Random rnd = new Random();

      for (int i = 1; i <= 5; i++) {
        JsonObject forecast = new JsonObject()
          .put("dateFormatted", now.plus(i, DAYS))
          .put("temperatureC", -20 + rnd.nextInt(35))
          .put("summary", SUMMARIES.get(rnd.nextInt(SUMMARIES.size())));

        forecast.put("temperatureF", 32 + (int) (forecast.getInteger("temperatureC") / 0.5556));

        response.add(forecast);
      }

      ctx.response()
        .putHeader("Content-Type", "application/json")
        .end(response.encode());
    });

    // Serve the static resources
    router.route().handler(HotReload.createStaticHandler());

    vertx.createHttpServer().requestHandler(router::accept).listen(8081, res -> {
      if (res.failed()) {
        res.cause().printStackTrace();
      } else {
        System.out.println("Server listening at: http://localhost:8081/");
      }
    });
  }
}
