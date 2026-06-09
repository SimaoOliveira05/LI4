package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.EstadoPagamentoRemessa;
import pt.trasmum.servidor.dominio.RemessaCentral;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.RemessaCentralRepositorio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RemessaCentralRepositorioImpl implements RemessaCentralRepositorio {
    private final DatabaseConnection db;

    public RemessaCentralRepositorioImpl(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public void guardar(RemessaCentral r) {
        try (Connection c = db.obter()) { guardar(c, r); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void guardar(Connection conn, RemessaCentral r) {
        String sql = "INSERT INTO RemessaCentral (idOriginalLoja, idLoja, nomeFornecedor, dataRecepcao, valorTotalGuia, estadoPagamento) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nomeFornecedor = VALUES(nomeFornecedor), dataRecepcao = VALUES(dataRecepcao), valorTotalGuia = VALUES(valorTotalGuia), estadoPagamento = VALUES(estadoPagamento)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdOriginalLoja());
            ps.setString(2, r.getIdLoja());
            ps.setString(3, r.getNomeFornecedor());
            ps.setDate(4, Date.valueOf(r.getDataRecepcao()));
            ps.setDouble(5, r.getValorTotalGuia());
            ps.setString(6, r.getEstadoPagamento().name());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void atualizarEstadoPagamento(Connection conn, String idLoja, int idOriginalRemessa, EstadoPagamentoRemessa estado) {
        String sql = "UPDATE RemessaCentral SET estadoPagamento = ? WHERE idLoja = ? AND idOriginalLoja = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setString(2, idLoja);
            ps.setInt(3, idOriginalRemessa);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private RemessaCentral mapear(ResultSet rs) throws SQLException {
        return new RemessaCentral(
                rs.getInt("id"),
                rs.getInt("idOriginalLoja"),
                rs.getString("idLoja"),
                rs.getString("nomeFornecedor"),
                rs.getDate("dataRecepcao").toLocalDate(),
                rs.getDouble("valorTotalGuia"),
                EstadoPagamentoRemessa.valueOf(rs.getString("estadoPagamento"))
        );
    }

    private static final String COLS = "id, idOriginalLoja, idLoja, nomeFornecedor, dataRecepcao, valorTotalGuia, estadoPagamento";

    @Override
    public List<RemessaCentral> listarTodas() {
        List<RemessaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM RemessaCentral ORDER BY dataRecepcao DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapear(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public List<RemessaCentral> buscarPorLoja(String idLoja) {
        List<RemessaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM RemessaCentral WHERE idLoja = ? ORDER BY dataRecepcao DESC")) {
            ps.setString(1, idLoja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public List<RemessaCentral> buscarPorEstado(EstadoPagamentoRemessa estado) {
        List<RemessaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM RemessaCentral WHERE estadoPagamento = ? ORDER BY dataRecepcao DESC")) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public double totalDespesasPorPeriodo(LocalDate inicio, LocalDate fim) {
        String sql = "SELECT COALESCE(SUM(valorTotalGuia), 0) FROM RemessaCentral WHERE dataRecepcao BETWEEN ? AND ?";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public double totalDespesasPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim) {
        String sql = "SELECT COALESCE(SUM(valorTotalGuia), 0) FROM RemessaCentral WHERE idLoja = ? AND dataRecepcao BETWEEN ? AND ?";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, idLoja);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<RemessaCentral> buscarPorLojaEEstado(String idLoja, EstadoPagamentoRemessa estado) {
        List<RemessaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM RemessaCentral WHERE idLoja = ? AND estadoPagamento = ? ORDER BY dataRecepcao DESC")) {
            ps.setString(1, idLoja);
            ps.setString(2, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }
}
