package pt.trasmum.loja.dominio.core;

public enum TipoAcao {
    VENDA("Venda"),
    DEVOLUCAO("Devolução"),
    ALTERACAO_PRECO("Alteração de preço"),
    APLICACAO_DESCONTO("Aplicação de desconto"),
    GESTAO_UTILIZADOR("Gestão de utilizador"),
    ALTERACAO_CATALOGO("Alteração de catálogo"),
    FECHO_DIA("Fecho de dia");

    private final String descricao;

    TipoAcao(String descricao) {
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
