package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.LogAuditoriaCentral;
import pt.trasmum.servidor.dominio.TipoAcao;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.LogAuditoriaCentralRepositorio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LogAuditoriaCentralRepositorioImpl implements LogAuditoriaCentralRepositorio {
    private final DatabaseConnection db;

    public LogAuditoriaCentralRepositorioImpl(DatabaseConnection db) { this.db = db; }

    @Override
    public void guardar(LogAuditoriaCentral l) {
        try (Connection c = db.obter()) { guardar(c, l); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void guardar(Connection conn, LogAuditoriaCentral l) {
        String sql = "INSERT INTO LogAuditoriaCentral (idOriginalLoja, idLoja, acao, dataHora, nomeUtilizador, descricao) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE acao = VALUES(acao), dataHora = VALUES(dataHora), nomeUtilizador = VALUES(nomeUtilizador), descricao = VALUES(descricao)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, l.getIdOriginalLoja());
            ps.setString(2, l.getIdLoja());
            ps.setString(3, l.getAcao().name());
            ps.setTimestamp(4, Timestamp.valueOf(l.getDataHora()));
            ps.setString(5, l.getNomeUtilizador());
            ps.setString(6, l.getDescricao());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private LogAuditoriaCentral mapear(ResultSet rs) throws SQLException {
        return new LogAuditoriaCentral(
                rs.getInt("id"),
                rs.getInt("idOriginalLoja"),
                rs.getString("idLoja"),
                TipoAcao.valueOf(rs.getString("acao")),
                rs.getTimestamp("dataHora").toLocalDateTime(),
                rs.getString("nomeUtilizador"),
                rs.getString("descricao")
        );
    }

    @Override
    public List<LogAuditoriaCentral> buscarPorLoja(String idLoja) {
        List<LogAuditoriaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idOriginalLoja, idLoja, acao, dataHora, nomeUtilizador, descricao FROM LogAuditoriaCentral WHERE idLoja = ? ORDER BY dataHora DESC")) {
            ps.setString(1, idLoja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public List<LogAuditoriaCentral> buscarPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim) {
        List<LogAuditoriaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idOriginalLoja, idLoja, acao, dataHora, nomeUtilizador, descricao FROM LogAuditoriaCentral WHERE idLoja = ? AND DATE(dataHora) BETWEEN ? AND ? ORDER BY dataHora DESC")) {
            ps.setString(1, idLoja);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }
}
