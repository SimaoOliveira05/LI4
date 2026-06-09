package pt.trasmum.loja.servico;

public record ProdutoDTO(String codigoBarras, String nome, String categoria,
                         double precoBase, int stockMinimo, boolean temValidade) {}
