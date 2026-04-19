package pt.trasmum.loja.sincronizacao;

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

    public static class VendaDTO {
        public int idOriginalLoja;
        public String dataHora;
        public double totalFaturado;
        public String metodoPagamento;
        public List<LinhaVendaDTO> linhas;
    }

    public static class LinhaVendaDTO {
        public int idOriginalLoja;
        public String nomeProduto;
        public String categoria;
        public int quantidade;
        public double precoUnitario;
        public double subtotal;
    }

    public static class DevolucaoDTO {
        public int idOriginalLoja;
        public int idOriginalVenda;
        public String dataHora;
        public int quantidade;
        public double valorRestituido;
    }

    public static class RemessaDTO {
        public int idOriginalLoja;
        public String nomeFornecedor;
        public String dataRecepcao;
        public double valorTotalGuia;
        public String estadoPagamento;
    }

    public static class PagamentoDTO {
        public int idOriginalLoja;
        public int idOriginalRemessa;
        public double valor;
        public String dataPagamento;
        public String tipoPagamento;
    }

    public static class SessaoCaixaDTO {
        public int idOriginalLoja;
        public int idUtilizador;
        public double saldoFinal;
        public String dataAbertura;
        public String dataEncerramento;
    }

    public static class LogAuditoriaDTO {
        public int idOriginalLoja;
        public String acao;
        public String dataHora;
        public String nomeUtilizador;
    }
}
