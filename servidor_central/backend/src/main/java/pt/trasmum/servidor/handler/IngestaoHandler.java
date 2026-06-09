package pt.trasmum.servidor.handler;

import io.javalin.http.Context;
import pt.trasmum.servidor.dto.ingestao.PacoteFechoDTO;
import pt.trasmum.servidor.servico.interfaces.IIngestaoServico;

import java.util.Map;

public class IngestaoHandler {
    private final IIngestaoServico ingestao;

    public IngestaoHandler(IIngestaoServico ingestao) {
        this.ingestao = ingestao;
    }

    public void receberPacote(Context ctx) {
        PacoteFechoDTO pacote = ctx.bodyAsClass(PacoteFechoDTO.class);
        ingestao.processarPacote(pacote);
        ctx.status(200).json(Map.of(
                "ack", true,
                "mensagem", "Pacote processado com sucesso"
        ));
    }
}
