package pt.trasmum.servidor.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseConnection(Properties config) {
        this.url = config.getProperty("db.url");
        this.user = config.getProperty("db.user");
        this.password = config.getProperty("db.password");
    }

    public Connection obter() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
