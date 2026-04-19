package pt.trasmum.servidor.handler;

import io.javalin.http.Context;
import pt.trasmum.servidor.servico.interfaces.IRelatorioServico;

import java.time.LocalDate;

public class RelatorioHandler {
    private final IRelatorioServico relatorio;

    public RelatorioHandler(IRelatorioServico relatorio) {
        this.relatorio = relatorio;
    }

    public void gerar(Context ctx) {
        String idLoja = ctx.queryParam("idLoja");
        String categoria = ctx.queryParam("categoria");
        LocalDate inicio = parse(ctx.queryParam("inicio"));
        LocalDate fim = parse(ctx.queryParam("fim"));
        ctx.json(relatorio.gerarRelatorio(idLoja, inicio, fim, categoria));
    }

    private LocalDate parse(String s) {
        return (s == null || s.isBlank()) ? null : LocalDate.parse(s);
    }
}
