package pt.trasmum.servidor.handler;

import io.javalin.http.Context;
import pt.trasmum.servidor.servico.interfaces.IDashboardServico;

import java.time.LocalDate;

public class DashboardHandler {
    private final IDashboardServico dashboard;

    public DashboardHandler(IDashboardServico dashboard) {
        this.dashboard = dashboard;
    }

    public void global(Context ctx) {
        LocalDate fim = parse(ctx.queryParam("fim"), LocalDate.now());
        LocalDate inicio = parse(ctx.queryParam("inicio"), fim.minusDays(30));
        ctx.json(dashboard.obterDashboardGlobal(inicio, fim));
    }

    public void loja(Context ctx) {
        String idLoja = ctx.pathParam("idLoja");
        LocalDate fim = parse(ctx.queryParam("fim"), LocalDate.now());
        LocalDate inicio = parse(ctx.queryParam("inicio"), fim.minusDays(30));
        ctx.json(dashboard.obterDashboardLoja(idLoja, inicio, fim));
    }

    private LocalDate parse(String s, LocalDate fallback) {
        return (s == null || s.isBlank()) ? fallback : LocalDate.parse(s);
    }
}
