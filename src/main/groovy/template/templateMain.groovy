package template

import io.vertx.core.Vertx

class templateMain {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx()
        vertx.deployVerticle(new Timer())
    }
}
