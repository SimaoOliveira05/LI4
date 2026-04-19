package pt.trasmum.servidor.dto.api;

public class LoginResponseDTO {
    public String token;
    public String nomeUtilizador;

    public LoginResponseDTO(String token, String nomeUtilizador) {
        this.token = token;
        this.nomeUtilizador = nomeUtilizador;
    }
}
