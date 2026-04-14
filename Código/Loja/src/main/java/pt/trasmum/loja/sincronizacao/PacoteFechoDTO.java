package pt.trasmum.loja.sincronizacao;

import java.time.LocalDate;
import java.util.List;

public class PacoteFechoDTO {
    public String idLoja;
    public String nomeLoja;
    public LocalDate dataFecho;
    public List<VendaDTO> vendas;
    public List<DevolucaoDTO> devolucoes;
    public List<RemessaDTO> remessas;
    public List<PagamentoDTO> pagamentos;
    public List<SessaoCaixaDTO> sessoesCaixa;
    public List<LogAuditoriaDTO> logs;
    public String hashIntegridade;

    // ---- DTOs internos ----

    public static class VendaDTO {
        public int id;
        public String idLoja;
        public int idUtilizador;
        public String dataHora;
        public String metodoPagamento;
        public double totalFaturado;
        public String estado;
        public String numeroFatura;
        public String nifCliente;
        public List<LinhaVendaDTO> linhas;
    }

    public static class LinhaVendaDTO {
        public int idLote;
        public int quantidade;
        public double precoUnitario;
    }

    public static class DevolucaoDTO {
        public int id;
        public String idLoja;
        public String numeroFatura;
        public int idUtilizador;
        public int idLote;
        public String dataHora;
        public int quantidade;
        public String dataValidadeEmbalagem;
        public double valorRestituido;
    }

    public static class RemessaDTO {
        public int id;
        public String idLoja;
        public int idFornecedor;
        public int idUtilizador;
        public String dataRecepcao;
        public double valorTotalGuia;
        public List<LinhaRemessaDTO> linhas;
    }

    public static class LinhaRemessaDTO {
        public int idProduto;
        public int idLoteGerado;
        public int quantidade;
        public String dataValidade;
    }

    public static class PagamentoDTO {
        public int id;
        public String idLoja;
        public int idFornecedor;
        public int idRemessa;
        public double valor;
        public String estadoPagamento;
        public String dataHora;
    }

    public static class SessaoCaixaDTO {
        public int id;
        public String idLoja;
        public int idUtilizador;
        public double saldoAtual;
        public String dataAbertura;
        public String dataEncerramento;
    }

    public static class LogAuditoriaDTO {
        public int id;
        public String idLoja;
        public String acao;
        public String dataHora;
        public int idUtilizador;
        public String entidade;
        public int idEntidade;
    }
}
