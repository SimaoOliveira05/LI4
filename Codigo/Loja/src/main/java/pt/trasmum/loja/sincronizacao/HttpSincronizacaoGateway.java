package pt.trasmum.loja.sincronizacao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.time.Duration;

public class HttpSincronizacaoGateway implements SincronizacaoGateway {

    private final String urlServidor;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpSincronizacaoGateway(String urlServidor, String truststorePath, String truststorePassword) {
        this.urlServidor = urlServidor;
        this.httpClient = construirHttpClient(truststorePath, truststorePassword);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(java.time.LocalDate.class,
                        new com.google.gson.JsonSerializer<java.time.LocalDate>() {
                            @Override
                            public com.google.gson.JsonElement serialize(java.time.LocalDate src,
                                    java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                                return new com.google.gson.JsonPrimitive(src.toString());
                            }
                        })
                .registerTypeAdapter(java.time.LocalDateTime.class,
                        new com.google.gson.JsonSerializer<java.time.LocalDateTime>() {
                            @Override
                            public com.google.gson.JsonElement serialize(java.time.LocalDateTime src,
                                    java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                                return new com.google.gson.JsonPrimitive(src.toString());
                            }
                        })
                .create();
    }

    @Override
    public boolean enviarFechoDia(PacoteFechoDTO pacote) {
        try {
            // Calcula hash antes de serializar o pacote completo
            String jsonSemHash = gson.toJson(pacote);
            pacote.hashIntegridade = sha256(jsonSemHash);

            String json = gson.toJson(pacote);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlServidor + "/fecho"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMinutes(5))
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean verificarConexao() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlServidor + "/ping"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private HttpClient construirHttpClient(String truststorePath, String truststorePassword) {
        try {
            if (truststorePath != null && !truststorePath.isBlank()) {
                KeyStore ks = KeyStore.getInstance("JKS");
                try (FileInputStream fis = new FileInputStream(truststorePath)) {
                    ks.load(fis, truststorePassword.toCharArray());
                }
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                SSLContext sslCtx = SSLContext.getInstance("TLS");
                sslCtx.init(null, tmf.getTrustManagers(), null);
                return HttpClient.newBuilder()
                        .sslContext(sslCtx)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao carregar truststore '" + truststorePath + "': " + e.getMessage(), e);
        }
        return construirHttpClientPermissivo();
    }

    private HttpClient construirHttpClientPermissivo() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new java.security.SecureRandom());
            return HttpClient.newBuilder()
                    .sslContext(sc)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        } catch (Exception e) {
            return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
