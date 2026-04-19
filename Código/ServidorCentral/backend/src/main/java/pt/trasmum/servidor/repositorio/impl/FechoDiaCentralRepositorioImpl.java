package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.FechoDiaCentral;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.FechoDiaCentralRepositorio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FechoDiaCentralRepositorioImpl implements FechoDiaCentralRepositorio {
    private final DatabaseConnection db;

    public FechoDiaCentralRepositorioImpl(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public void guardar(FechoDiaCentral fecho) {
        try (Connection c = db.obter()) {
            guardar(c, fecho);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar fecho: " + e.getMessage(), e);
        }
    }

    @Override
    public void guardar(Connection conn, FechoDiaCentral fecho) {
        String sql = "INSERT INTO FechoDiaCentral (idLoja, dataFecho, dataRecepcao) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE dataRecepcao = VALUES(dataRecepcao)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fecho.getIdLoja());
            ps.setDate(2, Date.valueOf(fecho.getDataFecho()));
            ps.setTimestamp(3, Timestamp.valueOf(fecho.getDataRecepcao()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar fecho: " + e.getMessage(), e);
        }
    }

    private FechoDiaCentral mapear(ResultSet rs) throws SQLException {
        return new FechoDiaCentral(
                rs.getInt("id"),
                rs.getString("idLoja"),
                rs.getDate("dataFecho").toLocalDate(),
                rs.getTimestamp("dataRecepcao").toLocalDateTime()
        );
    }

    @Override
    public List<FechoDiaCentral> buscarPorData(LocalDate data) {
        List<FechoDiaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idLoja, dataFecho, dataRecepcao FROM FechoDiaCentral WHERE dataFecho = ?")) {
            ps.setDate(1, Date.valueOf(data));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    @Override
    public FechoDiaCentral buscarPorLojaEData(String idLoja, LocalDate data) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idLoja, dataFecho, dataRecepcao FROM FechoDiaCentral WHERE idLoja = ? AND dataFecho = ?")) {
            ps.setString(1, idLoja);
            ps.setDate(2, Date.valueOf(data));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FechoDiaCentral> listarTodos() {
        List<FechoDiaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idLoja, dataFecho, dataRecepcao FROM FechoDiaCentral ORDER BY dataFecho DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    @Override
    public List<FechoDiaCentral> buscarPorLoja(String idLoja) {
        List<FechoDiaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idLoja, dataFecho, dataRecepcao FROM FechoDiaCentral WHERE idLoja = ? ORDER BY dataFecho DESC")) {
            ps.setString(1, idLoja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }
}
