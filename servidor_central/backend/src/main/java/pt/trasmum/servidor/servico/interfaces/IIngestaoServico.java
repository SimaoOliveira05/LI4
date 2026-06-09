package pt.trasmum.servidor.servico.interfaces;

import pt.trasmum.servidor.dto.ingestao.PacoteFechoDTO;

public interface IIngestaoServico {
    void processarPacote(PacoteFechoDTO pacote);
}
