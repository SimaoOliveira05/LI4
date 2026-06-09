package pt.trasmum.loja.dominio.tesouraria;

import pt.trasmum.loja.dominio.core.RegistoSincronizavel;

import java.time.LocalDate;

public class FechoDia extends RegistoSincronizavel {

    private int idUtilizador;
    private LocalDate dataFecho;

    public FechoDia() {
        super();
    }

    public FechoDia(String idLoja, int idUtilizador, LocalDate dataFecho) {
        super(idLoja);
        this.idUtilizador = idUtilizador;
        this.dataFecho = dataFecho;
    }

    public int getIdUtilizador() { return idUtilizador; }
    public void setIdUtilizador(int idUtilizador) { this.idUtilizador = idUtilizador; }

    public LocalDate getDataFecho() { return dataFecho; }
    public void setDataFecho(LocalDate dataFecho) { this.dataFecho = dataFecho; }
}
