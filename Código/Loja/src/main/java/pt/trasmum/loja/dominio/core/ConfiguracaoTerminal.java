package pt.trasmum.loja.dominio.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfiguracaoTerminal {

    private final String idLoja;
    private final String nomeLoja;
    private final String morada;
    private final String localidade;
    private final String nif;
    private final String email;
    private final double limiteMaximoCaixa;
    private final String urlServidor;
    private final int diasAlertaValidade;

    private ConfiguracaoTerminal(String idLoja, String nomeLoja, String morada, String localidade,
                                  String nif, String email, double limiteMaximoCaixa,
                                  String urlServidor, int diasAlertaValidade) {
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
        this.morada = morada;
        this.localidade = localidade;
        this.nif = nif;
        this.email = email;
        this.limiteMaximoCaixa = limiteMaximoCaixa;
        this.urlServidor = urlServidor;
        this.diasAlertaValidade = diasAlertaValidade;
    }

    public static ConfiguracaoTerminal carregar() {
        Properties props = new Properties();
        try (InputStream is = ClassLoader.getSystemResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RuntimeException("config.properties não encontrado no classpath");
            }
            props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar config.properties", e);
        }

        return new ConfiguracaoTerminal(
                props.getProperty("loja.id"),
                props.getProperty("loja.nome"),
                props.getProperty("loja.morada", ""),
                props.getProperty("loja.localidade", ""),
                props.getProperty("loja.nif", ""),
                props.getProperty("loja.email", ""),
                Double.parseDouble(props.getProperty("loja.limiteMaximoCaixa")),
                props.getProperty("servidor.url"),
                Integer.parseInt(props.getProperty("servidor.alertaValidade.dias"))
        );
    }

    public String getIdLoja() { return idLoja; }
    public String getNomeLoja() { return nomeLoja; }
    public String getMorada() { return morada; }
    public String getLocalidade() { return localidade; }
    public String getNif() { return nif; }
    public String getEmail() { return email; }
    public double getLimiteMaximoCaixa() { return limiteMaximoCaixa; }
    public String getUrlServidor() { return urlServidor; }
    public int getDiasAlertaValidade() { return diasAlertaValidade; }
}
