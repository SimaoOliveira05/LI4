package pt.trasmum.servidor.dto.ingestao;

import java.util.List;

public class PacoteFechoDTO {
    public String idLoja;
    public String nomeLoja;
    public String dataFecho;
    public String hashIntegridade;
    public List<VendaDTO> vendas;
    public List<DevolucaoDTO> devolucoes;
    public List<RemessaDTO> remessas;
    public List<PagamentoDTO> pagamentos;
    public List<SessaoCaixaDTO> sessoesCaixa;
    public List<LogAuditoriaDTO> logs;
}
