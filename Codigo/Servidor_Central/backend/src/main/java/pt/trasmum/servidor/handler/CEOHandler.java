package pt.trasmum.servidor.handler;

import io.javalin.http.Context;
import pt.trasmum.servidor.dto.api.CriarCEORequestDTO;
import pt.trasmum.servidor.servico.interfaces.IAutenticacaoCEOServico;

public class CEOHandler {
    private final IAutenticacaoCEOServico servico;

    public CEOHandler(IAutenticacaoCEOServico servico) {
        this.servico = servico;
    }

    public void criar(Context ctx) {
        CriarCEORequestDTO req = ctx.bodyAsClass(CriarCEORequestDTO.class);
        servico.criar(req.nomeUtilizador, req.palavraPasse);
        ctx.status(201);
    }
}
