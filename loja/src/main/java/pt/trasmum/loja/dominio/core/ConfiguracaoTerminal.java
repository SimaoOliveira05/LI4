package pt.trasmum.loja.dominio.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfiguracaoTerminal {

    private final String idLoja;
    private final String nomeLoja;
    private final double limiteMaximoCaixa;
    private final String urlServidor;
    private final int diasAlertaValidade;

    private ConfiguracaoTerminal(String idLoja, String nomeLoja, double limiteMaximoCaixa,
                                  String urlServidor, int diasAlertaValidade) {
        this.idLoja = idLoja;
        this.nomeLoja = nomeLoja;
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
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar config.properties", e);
        }

        return new ConfiguracaoTerminal(
                props.getProperty("loja.id"),
                props.getProperty("loja.nome"),
                Double.parseDouble(props.getProperty("loja.limiteMaximoCaixa")),
                props.getProperty("servidor.url"),
                Integer.parseInt(props.getProperty("servidor.alertaValidade.dias"))
        );
    }

    public String getIdLoja() { return idLoja; }
    public String getNomeLoja() { return nomeLoja; }
    public double getLimiteMaximoCaixa() { return limiteMaximoCaixa; }
    public String getUrlServidor() { return urlServidor; }
    public int getDiasAlertaValidade() { return diasAlertaValidade; }
}
