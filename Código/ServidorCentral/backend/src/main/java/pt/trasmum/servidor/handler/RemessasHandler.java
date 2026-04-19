package pt.trasmum.servidor.handler;

import io.javalin.http.Context;
import pt.trasmum.servidor.servico.interfaces.IRemessaServico;

public class RemessasHandler {
    private final IRemessaServico remessas;

    public RemessasHandler(IRemessaServico remessas) {
        this.remessas = remessas;
    }

    public void listar(Context ctx) {
        String idLoja = ctx.queryParam("idLoja");
        String estado = ctx.queryParam("estadoPagamento");
        ctx.json(remessas.listarRemessas(idLoja, estado));
    }
}
