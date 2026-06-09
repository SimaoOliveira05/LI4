package pt.trasmum.servidor.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private final String url;
    private final String user;
    private final String password;

    private final int maxTentativas;
    private final int delayMs;

    public DatabaseConnection(Properties config) {
        this.url = require(config, "db.url");
        this.user = require(config, "db.user");
        this.password = require(config, "db.password");
        this.maxTentativas = Integer.parseInt(config.getProperty("db.retry.maxTentativas", "10"));
        this.delayMs = Integer.parseInt(config.getProperty("db.retry.delayMs", "2000"));
    }

    private String require(Properties config, String key) {
        String value = config.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Config obrigatória em falta: " + key);
        }
        return value;
    }

    public Connection obter() throws SQLException {
        int tentativas = maxTentativas;

        while (true) {
            try {
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                if (--tentativas == 0) throw e;

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
