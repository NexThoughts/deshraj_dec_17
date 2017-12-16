package com.example;

import com.example.VO.TeamVO;
import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by deshraj on 27/11/17.
 */
public class MainVertical extends AbstractVerticle {

    private static final String MY_SQL_GET_PAGE = "select id, content from pages where name = ?";
    private static final String MY_SQL_CREATE_PAGE = "insert into pages values (NULL, ?, ?)";
    private static final String MY_SQL_SAVE_PAGE = "update pages set content = ? where id = ?";
    private static final String MY_SQL_ALL_PAGES = "select name from pages";
    private static final String MY_SQL_DELETE_PAGE = "delete from pages where id = ?";
    private static final String MY_SQL_CREATE_TASK_TABLE = "create table task(id MEDIUMINT(5) AUTO_INCREMENT, name VARCHAR(20), description VARCHAR(200), dueDate VARCHAR(20), assigneeId MEDIUMINT(5), status VARCHAR(20), teamId MEDIUMINT(5), PRIMARY KEY (id))";
    private static final String MY_SQL_CREATE_TEAM_TABLE = "create table team(id MEDIUMINT(5) AUTO_INCREMENT, name VARCHAR(20), adminId MEDIUMINT(5), PRIMARY KEY (id))";
    private static final String MY_SQL_CREATE_TEAM_USER_MAPPING_TABLE = "create table teamusermapping(teamId MEDIUMINT(5), userId MEDIUMINT(5))";


    private JDBCClient dbClient;
    private JDBCAuth authProvider;
    private MongoClient mongoClient;
    private final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();

    //    This is vertx future not JDK's future
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<Void> steps = prepareDatabase().compose(v -> startHttpServer());
        steps.setHandler(startFuture.completer());
    }


    private Future<Void> prepareDatabase() {
        Future<Void> future = Future.future();
//        try {
//            mongoClient = MongoClient.createShared(
//                    vertx,
//                    new JsonObject()
//                            .put("host", "localhost")
//                            .put("db_name", "demo_vertx")
//                            .put("max_pool_size", 30)
//            );
//        } catch (Exception e) {
//            System.out.println("===== There occurred an exception" + e);
//            e.printStackTrace();
//        }
//        mongoClient.dropCollection("task", res -> {
//            if (res.succeeded()) {
//                System.out.println("===== Dropped existing collection task successfully ========");
//            } else {
//                System.out.println("====== Couldn't delete existing collection task ========");
//            }
//        });
//        mongoClient.createCollection("task", res -> {
//            if (res.succeeded()) {
//                System.out.println("=== Successfully created task collection ====");
//            } else {
//                System.out.println("==== Unable to create task collection =====");
//                res.cause().printStackTrace();
//                future.fail(res.cause());
//            }
//        });
//        mongoClient.dropCollection("team", res -> {
//            if (res.succeeded()) {
//                System.out.println("===== Dropped existing collection team successfully ========");
//            } else {
//                System.out.println("====== Couldn't delete existing collection team ========");
//            }
//        });
//        mongoClient.createCollection("team", res -> {
//            if (res.succeeded()) {
//                System.out.println("=== Successfully created team collection ====");
//            } else {
//                System.out.println("==== Unable to create team collection =====");
//                res.cause().printStackTrace();
//                future.fail(res.cause());
//            }
//        });
        try {
            dbClient = JDBCClient.createShared(
                    vertx,
                    new JsonObject()
                            .put("url", "jdbc:mysql://localhost:3306/demo_vertx?autoreconnect=true")
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
                connection.execute(MY_SQL_CREATE_TEAM_TABLE, create -> {
                    if (create.failed()) {
                        System.out.println("Database preparation error" + create.cause());
                        future.fail(create.cause());
                    }
                });
                connection.execute(MY_SQL_CREATE_TASK_TABLE, create -> {
                    if (create.failed()) {
                        System.out.println("Database preparation error" + create.cause());
                        future.fail(create.cause());
                    }
                });
                connection.execute(MY_SQL_CREATE_TEAM_USER_MAPPING_TABLE, create -> {
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
        router.get("/dashboard").handler(this::dashboardHandler);
        router.get("/team/create").handler(this::createTeamHandler);
        router.post("/team/save").handler(this::saveTeamHandler);
        router.get("/task/create").handler(this::createTaskHandler);
        router.post("/task/save").handler(this::saveTaskHandler);
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

    private void createTeamHandler(RoutingContext context) {
        String userId = context.request().getParam("userId");
        context.put("username", "UserName");
        context.put("userId", userId);
        templateEngine.render(context, "templates", "/createTeam.ftl", ar -> {
            if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html");
                context.response().end(ar.result());
            } else {
                context.fail(ar.cause());
            }
        });
    }

    private void saveTeamHandler(RoutingContext context) {
        String userId = context.request().getParam("userId");
        String teamName = context.request().getParam("teamName");
        String users = context.request().getParam("users");
        List<String> userList = new ArrayList<String>();
        if (users != null && users.length() > 0) {
            userList = Arrays.asList(users.split(","));
        }
        List<Integer> userIdList = new ArrayList<Integer>();
        dbClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                String sql = "insert into team values (NULL, ?, ?)";
                JsonArray params = new JsonArray();
                params.add(teamName).add(userId);
                connection.updateWithParams(sql, params, res -> {
                    connection.close();
                    if (res.succeeded()) {
                        System.out.println("team created successfully");
                    }
                });
            }
        });
        if (userList.size() > 0) {
            for (String userName : userList) {
                dbClient.getConnection(car -> {
                    if (car.succeeded()) {
                        SQLConnection connection = car.result();
                        connection.query("select * from user where username = " + userName, res -> {
                            if (res.succeeded()) {
                                JsonObject object = res.result().toJson();
                                Integer memberUserId = object.getInteger("id");
                                userIdList.add(memberUserId);
                            }
                        });
                    }
                });
            }
        }
        if (userList.size() > 0) {
            dbClient.getConnection(car -> {
                if (car.succeeded()) {
                    SQLConnection connection = car.result();
                    String sql = "select * from team where name = ? and adminId = ?";
                    JsonArray params = new JsonArray();
                    params.add(teamName).add(userId);
                    connection.queryWithParams(sql, params, res -> {
                        if (res.succeeded()) {
                            JsonObject object = res.result().toJson();
                            Integer teamId = object.getInteger("id");
                            for (Integer memberId : userIdList) {
                                JsonArray param = new JsonArray();
                                params.add(teamId).add(memberId);
                                connection.updateWithParams("insert into teamusermapping values (? , ?)", param, response -> {
                                    if (response.succeeded()) {
                                        System.out.println("created team user mapping successfully");
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
        context.put("username", "UserName");
        context.put("userId", userId);
        templateEngine.render(context, "templates", "/createTeam.ftl", ar -> {
            if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html");
                context.response().end(ar.result());
            } else {
                context.fail(ar.cause());
            }
        });
    }


    private void createTaskHandler(RoutingContext context) {
        String teamId = context.request().getParam("teamId");
        context.put("taskname", "TaskName");
        context.put("teamId", teamId);
        templateEngine.render(context, "templates", "/createTask.ftl", ar -> {
            if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html");
                context.response().end(ar.result());
            } else {
                context.fail(ar.cause());
            }
        });
    }


    private void saveTaskHandler(RoutingContext context) {
        String teamId = context.request().getParam("teamId");
        String taskName = context.request().getParam("taskName");
        String description = context.request().getParam("description");
        String assignee = context.request().getParam("userName");
        String dueDate = context.request().getParam("dueDate");
        dbClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                String sql = "insert into task values (NULL, ?, ?)";
                JsonArray params = new JsonArray();
                params.add(taskName).add(description).add(dueDate).add(assignee).add(teamId);
                connection.updateWithParams(sql, params, res -> {
                    connection.close();
                    if (res.succeeded()) {
                        System.out.println("Task created successfully");
                    }
                });
            }
        });
        context.put("taskName", "TaskName");
        context.put("teamId", teamId);
        templateEngine.render(context, "templates", "/createTask.ftl", ar -> {
            if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html");
                context.response().end(ar.result());
            } else {
                context.fail(ar.cause());
            }
        });
    }

    private void dashboardHandler(RoutingContext context) {
        String userId = context.request().getParam("userId");
        dbClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                connection.query("select teamId from teamusermapping where userId = " + userId, res -> {
                    if (res.succeeded()) {
                        List<Integer> teamIds = res.result()
                                .getResults()
                                .stream()
                                .map(json -> json.getInteger(0))
                                .sorted()
                                .collect(Collectors.toList());
                        List<TeamVO> teamVOList = new ArrayList<TeamVO>();
                        List<TeamVO> owningTeamList = new ArrayList<TeamVO>();
                        connection.query("select * from team where adminId = " + userId, response -> {
                            if (response.succeeded()) {
                                for (JsonObject object : response.result().getRows()) {
                                    TeamVO teamVO = new TeamVO(object);
                                    teamVOList.add(teamVO);
                                }
                            }
                        });
                        for (Integer teamId : teamIds) {
                            connection.query("select * from team where id = " + teamId, res1 -> {
                                if (res1.succeeded()) {
                                    JsonObject object = res1.result().toJson();
                                    TeamVO teamVO = new TeamVO(object);
                                    teamVOList.add(teamVO);
                                }
                            });
                        }
                        context.put("username", "UserName");
                        context.put("memberTeamList", teamVOList);
                        context.put("owningTeamList", owningTeamList);
                        templateEngine.render(context, "templates", "/dashboard.ftl", ar -> {
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


//    private Future<Void> startHttpServer() {
//        Future<Void> future = Future.future();
//        HttpServer server = vertx.createHttpServer();
//        Router router = Router.router(vertx);
//        router.get("/").handler(this::indexHandler);
//        router.get("/wiki/:page").handler(this::pageRenderingHandler);
//        router.post().handler(BodyHandler.create());
//        router.post("/save").handler(this::pageUpdateHandler);
//        router.post("/create").handler(this::pageCreateHandler);
//        router.post("/delete").handler(this::pageDeletionHandler);
//        server.requestHandler(router::accept)
//                .listen(8080, ar -> {
//                    if (ar.succeeded()) {
//                        System.out.println("HTTP server running on port 8080");
//                        future.complete();
//                    } else {
//                        System.out.println("Could not start a HTTP server" + ar.cause());
//                        future.fail(ar.cause());
//                    }
//                });
//        return future;
//    }

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
