package pt.trasmum.servidor.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FechoDiaCentral {
    private int id;
    private String idLoja;
    private LocalDate dataFecho;
    private LocalDateTime dataRecepcao;

    public FechoDiaCentral() {}

    public FechoDiaCentral(int id, String idLoja, LocalDate dataFecho, LocalDateTime dataRecepcao) {
        this.id = id;
        this.idLoja = idLoja;
        this.dataFecho = dataFecho;
        this.dataRecepcao = dataRecepcao;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getIdLoja() { return idLoja; }
    public void setIdLoja(String idLoja) { this.idLoja = idLoja; }
    public LocalDate getDataFecho() { return dataFecho; }
    public void setDataFecho(LocalDate dataFecho) { this.dataFecho = dataFecho; }
    public LocalDateTime getDataRecepcao() { return dataRecepcao; }
    public void setDataRecepcao(LocalDateTime dataRecepcao) { this.dataRecepcao = dataRecepcao; }
}
