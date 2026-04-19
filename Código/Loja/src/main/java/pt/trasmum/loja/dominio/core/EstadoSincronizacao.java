package pt.trasmum.loja.dominio.core;

public enum EstadoSincronizacao {
    PENDENTE("Pendente"),
    EM_TRANSITO("Em trânsito"),
    CONFIRMADO("Confirmado");

    private final String descricao;

    EstadoSincronizacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
