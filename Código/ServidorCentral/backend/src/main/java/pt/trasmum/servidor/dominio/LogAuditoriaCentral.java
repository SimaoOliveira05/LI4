package pt.trasmum.servidor.dominio;

import java.time.LocalDateTime;

public class LogAuditoriaCentral {
    private int id;
    private int idOriginalLoja;
    private String idLoja;
    private TipoAcao acao;
    private LocalDateTime dataHora;
    private String nomeUtilizador;

    public LogAuditoriaCentral() {}

    public LogAuditoriaCentral(int id, int idOriginalLoja, String idLoja, TipoAcao acao,
                               LocalDateTime dataHora, String nomeUtilizador) {
        this.id = id;
        this.idOriginalLoja = idOriginalLoja;
        this.idLoja = idLoja;
        this.acao = acao;
        this.dataHora = dataHora;
        this.nomeUtilizador = nomeUtilizador;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdOriginalLoja() { return idOriginalLoja; }
    public void setIdOriginalLoja(int idOriginalLoja) { this.idOriginalLoja = idOriginalLoja; }
    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public TipoAcao getAcao() { return acao; }
    public void setAcao(TipoAcao acao) { this.acao = acao; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getNomeUtilizador() { return nomeUtilizador; }
    public void setNomeUtilizador(String nomeUtilizador) { this.nomeUtilizador = nomeUtilizador; }
}
