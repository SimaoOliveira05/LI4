package pt.trasmum.servidor.app;

import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import pt.trasmum.servidor.dominio.ContaBloqueadaException;
import pt.trasmum.servidor.dominio.CredenciaisInvalidasException;
import pt.trasmum.servidor.dominio.IntegridadeInvalidaException;

import java.lang.reflect.Type;
import java.util.Map;

public class ServerMain {
    public static void main(String[] args) {
        AppContext ctx = AppContext.initialize();
        String origemFrontend = ctx.config.getProperty("frontend.origem", "http://localhost:5173");

        JsonMapper gsonMapper = new JsonMapper() {
            @NotNull @Override
            public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return ctx.gson.toJson(obj, type);
            }
            @NotNull @Override
            public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return ctx.gson.fromJson(json, targetType);
            }
        };

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(gsonMapper);
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    rule.allowHost(origemFrontend);
                    rule.allowCredentials = true;
                });
            });
        });

        app.before("/api/*", ctx.authHandler::verificarToken);

        app.post("/fecho", ctx.ingestaoHandler::receberPacote);
        app.get("/ping", c -> c.json(Map.of("ok", true)));

        app.post("/api/auth/login", ctx.authHandler::login);
        app.post("/api/auth/logout", ctx.authHandler::logout);

        app.get("/api/dashboard/global", ctx.dashboardHandler::global);
        app.get("/api/dashboard/loja/{idLoja}", ctx.dashboardHandler::loja);
        app.get("/api/monitor", ctx.monitorHandler::estado);
        app.get("/api/remessas", ctx.remessasHandler::listar);
        app.get("/api/relatorios", ctx.relatorioHandler::gerar);
        app.get("/api/lojas", ctx.lojaHandler::listar);

        app.exception(CredenciaisInvalidasException.class, (e, c) ->
                c.status(401).json(Map.of("erro", e.getMessage())));
        app.exception(ContaBloqueadaException.class, (e, c) ->
                c.status(423).json(Map.of("erro", e.getMessage())));
        app.exception(IntegridadeInvalidaException.class, (e, c) ->
                c.status(400).json(Map.of("ack", false, "erro", e.getMessage())));
        app.exception(IllegalArgumentException.class, (e, c) ->
                c.status(400).json(Map.of("erro", e.getMessage())));
        app.exception(Exception.class, (e, c) -> {
            e.printStackTrace();
            c.status(500).json(Map.of("erro", "Erro interno: " + e.getMessage()));
        });

        int porta = Integer.parseInt(ctx.config.getProperty("servidor.porta", "8080"));
        app.start(porta);
    }
}
