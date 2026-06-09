package pt.trasmum.loja.dominio.fornecedores;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoRemessa {

    public enum EstadoPedido { PENDENTE, CONCLUIDO }

    private int id;
    private int idFornecedor;
    private int idUtilizador;
    private LocalDate dataCriacao;
    private EstadoPedido estado;
    private List<LinhaPedidoRemessa> linhas;

    public PedidoRemessa() {
        this.estado = EstadoPedido.PENDENTE;
        this.linhas = new ArrayList<>();
    }

    public PedidoRemessa(int idFornecedor, int idUtilizador, LocalDate dataCriacao) {
        this.idFornecedor = idFornecedor;
        this.idUtilizador = idUtilizador;
        this.dataCriacao = dataCriacao;
        this.estado = EstadoPedido.PENDENTE;
        this.linhas = new ArrayList<>();
    }

    public void adicionarLinha(LinhaPedidoRemessa linha) {
        this.linhas.add(linha);
    }

    public boolean estaPendente() {
        return this.estado == EstadoPedido.PENDENTE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdFornecedor() { return idFornecedor; }
    public void setIdFornecedor(int idFornecedor) { this.idFornecedor = idFornecedor; }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public List<LinhaPedidoRemessa> getLinhas() { return linhas; }
    public void setLinhas(List<LinhaPedidoRemessa> linhas) { this.linhas = linhas; }
}
