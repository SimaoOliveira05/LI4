package pt.trasmum.loja.dominio.core;

public abstract class RegistoSincronizavel {

    protected int id;
    protected EstadoSincronizacao estadoSincronizacao;
    protected String idLoja;

    protected RegistoSincronizavel() {
        this.estadoSincronizacao = EstadoSincronizacao.PENDENTE;
    }

    protected RegistoSincronizavel(String idLoja) {
        this.idLoja = idLoja;
        this.estadoSincronizacao = EstadoSincronizacao.PENDENTE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdLoja() {
        return idLoja;
    }

    public void setIdLoja(String idLoja) {
        this.idLoja = idLoja;
    }

    public EstadoSincronizacao getEstadoSincronizacao() {
        return estadoSincronizacao;
    }

    public void setEstadoSincronizacao(EstadoSincronizacao estadoSincronizacao) {
        this.estadoSincronizacao = estadoSincronizacao;
    }

    public void marcarEmTransito() {
        this.estadoSincronizacao = EstadoSincronizacao.EM_TRANSITO;
    }

    public void marcarConfirmado() {
        this.estadoSincronizacao = EstadoSincronizacao.CONFIRMADO;
    }

    public void reverterParaPendente() {
        this.estadoSincronizacao = EstadoSincronizacao.PENDENTE;
    }
}
