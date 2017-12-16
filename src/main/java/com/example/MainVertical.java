package com.example;

import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by deshraj on 27/11/17.
 */
public class MainVertical extends AbstractVerticle {

    private static final String SQL_CREATE_PAGES_TABLE = "create table if not exists Pages (Id integer identity primary key,Name varchar(255) unique, Content clob)";
    private static final String MY_SQL_CREATE_PAGES_TABLE = "create table pages(id MEDIUMINT(5) AUTO_INCREMENT, name VARCHAR(20), content VARCHAR(200), PRIMARY KEY (id))";
    private static final String SQL_GET_PAGE = "select Id, Content from Pages where Name = ?";
    private static final String MY_SQL_GET_PAGE = "select id, content from pages where name = ?";
    private static final String SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)";
    private static final String MY_SQL_CREATE_PAGE = "insert into pages values (NULL, ?, ?)";
    private static final String SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?";
    private static final String MY_SQL_SAVE_PAGE = "update pages set content = ? where id = ?";
    private static final String SQL_ALL_PAGES = "select Name from Pages";
    private static final String MY_SQL_ALL_PAGES = "select name from pages";
    private static final String SQL_DELETE_PAGE = "delete from Pages where Id = ?";
    private static final String MY_SQL_DELETE_PAGE = "delete from pages where id = ?";

    private JDBCClient dbClient;
    private JDBCAuth authProvider;

//    private static final Logger LOGGER = LoggerFactory.getLogger(com.example.MainVertical.class);

    private final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();

    //    This is vertx future not JDK's future
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<Void> steps = prepareMySqlDatabase().compose(v -> startHttpServer());
        steps.setHandler(startFuture.completer());
    }

    public void sendMail(){
        MailConfig config = new MailConfig();
        MailClient mailClient = MailClient.createShared(vertx, config, "exampleclient");
    }

    /*private Future<Void> prepareDatabase() {
        Future<Void> future = Future.future();
        System.out.println("====== value fo vertx = " + vertx);
        System.out.println("====== value fo vertx = " + vertx.toString());
        JDBCClient dbClient = null;
        try {
            dbClient = JDBCClient.createShared(
                    vertx,
                    new JsonObject()
                            .put("url", "jdbc:hsqldb:file:db/wiki")
                            .put("driver_class", "org.hsqldb.jdbcDriver")
                            .put("max_pool_size", 30));
        } catch (Exception e) {
            System.out.println("===== There occurred an exception" + e);
            e.printStackTrace();
        }
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                System.out.println("Could not open a database connection" + ar.cause());
                future.fail(ar.cause());
            } else {
                SQLConnection connection = ar.result();
                connection.execute(SQL_CREATE_PAGES_TABLE, create -> {
                    connection.close();
                    if (create.failed()) {
                        System.out.println("Database preparation error" + create.cause());
                        future.fail(create.cause());
                    } else {
                        future.complete();
                    }
                });
            }
        });
        return future;
    }*/

    private Future<Void> prepareMySqlDatabase() {
        Future<Void> future = Future.future();
        System.out.println("====== value fo vertx = " + vertx);
        System.out.println("====== value fo vertx = " + vertx.toString());
        try {
            dbClient = JDBCClient.createShared(
                    vertx,
                    new JsonObject()
                            .put("host", "jdbc:mysql://localhost:3306/demo_vertx?autoreconnect=true")
                            .put("user", "root")
                            .put("password", "nextdefault")
                            .put("driver_class", "com.mysql.jdbc.Driver")
                            .put("max_pool_size", 30));
        } catch (Exception e) {
            System.out.println("===== There occurred an exception" + e);
            e.printStackTrace();
        }
        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                System.out.println("Could not open a database connection" + ar.cause());
                future.fail(ar.cause());
            } else {
                SQLConnection connection = ar.result();
                connection.execute(MY_SQL_CREATE_PAGES_TABLE, create -> {
                    connection.close();
                    if (create.failed()) {
                        System.out.println("Database preparation error" + create.cause());
                        future.fail(create.cause());
                    } else {
                        future.complete();
                    }
                });
            }
        });
        return future;
    }

    private Future<Void> startHttpServer() {
        Future<Void> future = Future.future();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.get("/").handler(this::indexHandler);
        router.get("/wiki/:page").handler(this::pageRenderingHandler);
        router.post().handler(BodyHandler.create());
        router.post("/save").handler(this::pageUpdateHandler);
        router.post("/create").handler(this::pageCreateHandler);
        router.post("/delete").handler(this::pageDeletionHandler);
        server.requestHandler(router::accept)
                .listen(8080, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("HTTP server running on port 8080");
                        future.complete();
                    } else {
                        System.out.println("Could not start a HTTP server" + ar.cause());
                        future.fail(ar.cause());
                    }
                });
        return future;
    }

    private void indexHandler(RoutingContext context) {
        dbClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                connection.query(MY_SQL_ALL_PAGES, res -> {
                    connection.close();

                    if (res.succeeded()) {
                        List<String> pages = res.result()
                                .getResults()
                                .stream()
                                .map(json -> json.getString(0))
                                .sorted()
                                .collect(Collectors.toList());
                        context.put("title", "Wiki home");
                        context.put("pages", pages);
                        templateEngine.render(context, "templates", "/index.ftl", ar -> {
                            if (ar.succeeded()) {
                                context.response().putHeader("Content-Type", "text/html");
                                context.response().end(ar.result());
                            } else {
                                context.fail(ar.cause());
                            }
                        });
                    } else {
                        context.fail(res.cause());
                    }
                });
            } else {
                context.fail(car.cause());
            }
        });
    }

    private static final String EMPTY_PAGE_MARKDOWN = "# A new page\n" + "\n" + "Feel-free to write in Markdown!\n";

    private void pageRenderingHandler(RoutingContext context) {
        String page = context.request().getParam("page");
        dbClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                connection.queryWithParams(MY_SQL_GET_PAGE, new JsonArray().add(page), fetch -> {
                    connection.close();
                    if (fetch.succeeded()) {
                        JsonArray row = fetch.result().getResults()
                                .stream()
                                .findFirst()
                                .orElseGet(() -> new JsonArray().add(-1).add(EMPTY_PAGE_MARKDOWN));
                        Integer id = row.getInteger(0);
                        String rawContent = row.getString(1);
                        context.put("title", page);
                        context.put("id", id);
                        context.put("newPage", fetch.result().getResults().size() == 0 ? "yes" : "no");
                        context.put("rawContent", rawContent);
                        context.put("content", Processor.process(rawContent));
                        context.put("timestamp", new Date().toString());
                        templateEngine.render(context, "templates", "/page.ftl", ar -> {
                            if (ar.succeeded()) {
                                context.response().putHeader("Content-Type", "text/html");
                                context.response().end(ar.result());
                            } else {
                                context.fail(ar.cause());
                            }
                        });
                    } else {
                        context.fail(fetch.cause());
                    }
                });
            } else {
                context.fail(car.cause());

            }
        });
    }

    private void pageCreateHandler(RoutingContext context) {
        String pageName = context.request().getParam("name");
        String location = "/wiki/" + pageName;
        if (pageName == null || pageName.isEmpty()) {

            location = "/";
        }
        context.response().setStatusCode(303);
        context.response().putHeader("Location", location);
        context.response().end();
    }

    private void pageUpdateHandler(RoutingContext context) {
        String id = context.request().getParam("id");
        String title = context.request().getParam("title");
        String markdown = context.request().getParam("markdown");
        boolean newPage = "yes".equals(context.request().getParam("newPage"));
        dbClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                String sql = newPage ? MY_SQL_CREATE_PAGE : MY_SQL_SAVE_PAGE;
                JsonArray params = new JsonArray();
                if (newPage) {
                    params.add(title).add(markdown);
                } else {
                    params.add(markdown).add(id);
                }
                connection.updateWithParams(sql, params, res -> {
                    connection.close();
                    if (res.succeeded()) {
                        context.response().setStatusCode(303);
                        context.response().putHeader("Location", "/wiki/" + title);
                        context.response().end();
                    } else {
                        context.fail(res.cause());
                    }
                });
            } else {
                context.fail(car.cause());
            }
        });
    }

    private void pageDeletionHandler(RoutingContext context) {
        String id = context.request().getParam("id");
        dbClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                connection.updateWithParams(MY_SQL_DELETE_PAGE, new JsonArray().add(id), res -> {
                    connection.close();
                    if (res.succeeded()) {
                        context.response().setStatusCode(303);
                        context.response().putHeader("Location", "/");
                        context.response().end();
                    } else {
                        context.fail(res.cause());
                    }
                });
            } else {
                context.fail(car.cause());
            }
        });
    }


}
