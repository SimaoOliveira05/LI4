package pt.trasmum.loja.dominio.catalogo;

public class Produto {

    private int id;
    private String codigoBarras;
    private String nome;
    private String categoria;
    private double precoBase;
    private int stockMinimo;
    private boolean temValidade;
    private boolean ativo;

    public Produto() {}

    public Produto(String codigoBarras, String nome, String categoria, double precoBase, int stockMinimo) {
        this(codigoBarras, nome, categoria, precoBase, stockMinimo, true);
    }

    public Produto(String codigoBarras, String nome, String categoria, double precoBase, int stockMinimo, boolean temValidade) {
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.categoria = categoria;
        this.precoBase = precoBase;
        this.stockMinimo = stockMinimo;
        this.temValidade = temValidade;
        this.ativo = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecoBase() { return precoBase; }
    public void setPrecoBase(double precoBase) { this.precoBase = precoBase; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isTemValidade() { return temValidade; }
    public void setTemValidade(boolean temValidade) { this.temValidade = temValidade; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
