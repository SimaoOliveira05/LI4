package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.DevolucaoCentral;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.DevolucaoCentralRepositorio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DevolucaoCentralRepositorioImpl implements DevolucaoCentralRepositorio {
    private final DatabaseConnection db;

    public DevolucaoCentralRepositorioImpl(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public void guardar(DevolucaoCentral d) {
        try (Connection c = db.obter()) {
            guardar(c, d);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void guardar(Connection conn, DevolucaoCentral d) {
        String sql = "INSERT INTO DevolucaoCentral (idOriginalLoja, idOriginalVenda, idLoja, dataHora, quantidade, valorRestituido) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE idOriginalVenda = VALUES(idOriginalVenda), dataHora = VALUES(dataHora), quantidade = VALUES(quantidade), valorRestituido = VALUES(valorRestituido)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getIdOriginalLoja());
            ps.setInt(2, d.getIdOriginalVenda());
            ps.setString(3, d.getIdLoja());
            ps.setTimestamp(4, Timestamp.valueOf(d.getDataHora()));
            ps.setInt(5, d.getQuantidade());
            ps.setDouble(6, d.getValorRestituido());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private DevolucaoCentral mapear(ResultSet rs) throws SQLException {
        return new DevolucaoCentral(
                rs.getInt("id"),
                rs.getInt("idOriginalLoja"),
                rs.getInt("idOriginalVenda"),
                rs.getString("idLoja"),
                rs.getTimestamp("dataHora").toLocalDateTime(),
                rs.getInt("quantidade"),
                rs.getDouble("valorRestituido")
        );
    }

    @Override
    public List<DevolucaoCentral> buscarPorLoja(String idLoja) {
        List<DevolucaoCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idOriginalLoja, idOriginalVenda, idLoja, dataHora, quantidade, valorRestituido FROM DevolucaoCentral WHERE idLoja = ?")) {
            ps.setString(1, idLoja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public double totalDevolucoesPorPeriodo(LocalDate inicio, LocalDate fim) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(valorRestituido), 0) FROM DevolucaoCentral WHERE DATE(dataHora) BETWEEN ? AND ?")) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public double totalDevolucoesPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(valorRestituido), 0) FROM DevolucaoCentral WHERE idLoja = ? AND DATE(dataHora) BETWEEN ? AND ?")) {
            ps.setString(1, idLoja);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
