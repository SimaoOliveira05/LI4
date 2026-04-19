package pt.trasmum.servidor.dto.api;

public class RelatorioLinhaDTO {
    public String data;
    public String idLoja;
    public String nomeLoja;
    public String categoria;
    public double vendas;

    public RelatorioLinhaDTO() {}

    public RelatorioLinhaDTO(String data, String idLoja, String nomeLoja, String categoria, double vendas) {
        this.data = data;
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
        this.categoria = categoria;
        this.vendas = vendas;
    }
}
