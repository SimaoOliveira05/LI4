package pt.trasmum.loja.dominio.tesouraria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sangria {

    private int id;
    private int idSessaoCaixa;
    private LocalDateTime dataHora;
    private List<DetalheNumerario> valor;

    public Sangria() {
        this.valor = new ArrayList<>();
        this.dataHora = LocalDateTime.now();
    }

    public Sangria(int idSessaoCaixa, List<DetalheNumerario> valor) {
        this.idSessaoCaixa = idSessaoCaixa;
        this.valor = valor != null ? valor : new ArrayList<>();
        this.dataHora = LocalDateTime.now();
    }

    public double getTotal() {
        return valor.stream().mapToDouble(DetalheNumerario::getSubtotal).sum();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdSessaoCaixa() { return idSessaoCaixa; }
    public void setIdSessaoCaixa(int idSessaoCaixa) { this.idSessaoCaixa = idSessaoCaixa; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public List<DetalheNumerario> getValor() { return valor; }
    public void setValor(List<DetalheNumerario> valor) { this.valor = valor; }
}
