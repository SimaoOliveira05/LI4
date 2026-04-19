package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.SessaoCaixaCentral;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.SessaoCaixaCentralRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessaoCaixaCentralRepositorioImpl implements SessaoCaixaCentralRepositorio {
    private final DatabaseConnection db;

    public SessaoCaixaCentralRepositorioImpl(DatabaseConnection db) { this.db = db; }

    @Override
    public void guardar(SessaoCaixaCentral s) {
        try (Connection c = db.obter()) { guardar(c, s); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void guardar(Connection conn, SessaoCaixaCentral s) {
        String sql = "INSERT INTO SessaoCaixaCentral (idOriginalLoja, idLoja, idUtilizador, saldoFinal, dataAbertura, dataEncerramento) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE idUtilizador = VALUES(idUtilizador), saldoFinal = VALUES(saldoFinal), dataAbertura = VALUES(dataAbertura), dataEncerramento = VALUES(dataEncerramento)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getIdOriginalLoja());
            ps.setString(2, s.getIdLoja());
            ps.setInt(3, s.getIdUtilizador());
            ps.setDouble(4, s.getSaldoFinal());
            ps.setTimestamp(5, Timestamp.valueOf(s.getDataAbertura()));
            ps.setTimestamp(6, Timestamp.valueOf(s.getDataEncerramento()));
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<SessaoCaixaCentral> buscarPorLoja(String idLoja) {
        List<SessaoCaixaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idOriginalLoja, idLoja, idUtilizador, saldoFinal, dataAbertura, dataEncerramento FROM SessaoCaixaCentral WHERE idLoja = ?")) {
            ps.setString(1, idLoja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SessaoCaixaCentral(
                            rs.getInt("id"),
                            rs.getInt("idOriginalLoja"),
                            rs.getString("idLoja"),
                            rs.getInt("idUtilizador"),
                            rs.getDouble("saldoFinal"),
                            rs.getTimestamp("dataAbertura").toLocalDateTime(),
                            rs.getTimestamp("dataEncerramento").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }
}
