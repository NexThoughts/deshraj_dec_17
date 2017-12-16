package template

import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine

class Timer extends AbstractVerticle {

    public void start() throws Exception {
        Router router = Router.router(vertx)
        router.route("/home").handler({ routingContext ->
            routingContext.vertx().setTimer(5000, { tid ->
                println("=================VERTEX !=======")
                routingContext.next()
            })
            print("===================NEW VERTX==========")
        })

        router.get().handler({ ctx ->
            ctx.vertx().setTimer(5000, { tid ->
                println("=================HELLO=======")
                ctx.next()
            })
        })
        vertx.createHttpServer().requestHandler(router.&accept).listen(8090)
    }
}
