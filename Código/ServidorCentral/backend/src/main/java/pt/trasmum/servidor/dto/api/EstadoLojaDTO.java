package pt.trasmum.servidor.dto.api;

public class EstadoLojaDTO {
    public String idLoja;
    public String nomeLoja;
    public String estado;
    public String horaRecepcao;

    public EstadoLojaDTO() {}

    public EstadoLojaDTO(String idLoja, String nomeLoja, String estado, String horaRecepcao) {
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
        this.estado = estado;
        this.horaRecepcao = horaRecepcao;
    }
}
