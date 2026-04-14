package pt.trasmum.loja.dominio.core;

import java.time.LocalDateTime;

public class LogAuditoria extends RegistoSincronizavel {

    private TipoAcao acao;
    private LocalDateTime dataHora;
    private int idUtilizador;
    private String entidade;
    private int idEntidade;

    public LogAuditoria() {
        super();
    }

    public LogAuditoria(String idLoja, TipoAcao acao, int idUtilizador, String entidade, int idEntidade) {
        super(idLoja);
        this.acao = acao;
        this.idUtilizador = idUtilizador;
        this.entidade = entidade;
        this.idEntidade = idEntidade;
        this.dataHora = LocalDateTime.now();
    }

    public TipoAcao getAcao() { return acao; }
    public void setAcao(TipoAcao acao) { this.acao = acao; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public String getEntidade() { return entidade; }
    public void setEntidade(String entidade) { this.entidade = entidade; }

    public int getIdEntidade() { return idEntidade; }
    public void setIdEntidade(int idEntidade) { this.idEntidade = idEntidade; }
}
