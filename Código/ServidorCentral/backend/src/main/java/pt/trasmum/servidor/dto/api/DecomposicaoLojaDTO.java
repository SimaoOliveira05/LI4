package pt.trasmum.servidor.dto.api;

public class DecomposicaoLojaDTO {
    public String idLoja;
    public String nomeLoja;
    public double vendas;
    public int transacoes;
    public double devolucoes;

    public DecomposicaoLojaDTO() {}

    public DecomposicaoLojaDTO(String idLoja, String nomeLoja, double vendas, int transacoes, double devolucoes) {
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
        this.vendas = vendas;
        this.transacoes = transacoes;
        this.devolucoes = devolucoes;
    }
}
