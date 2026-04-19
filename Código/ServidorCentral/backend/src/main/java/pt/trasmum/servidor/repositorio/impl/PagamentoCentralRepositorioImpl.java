package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.PagamentoCentral;
import pt.trasmum.servidor.dominio.TipoPagamento;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.PagamentoCentralRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoCentralRepositorioImpl implements PagamentoCentralRepositorio {
    private final DatabaseConnection db;

    public PagamentoCentralRepositorioImpl(DatabaseConnection db) { this.db = db; }

    @Override
    public void guardar(PagamentoCentral p) {
        try (Connection c = db.obter()) { guardar(c, p); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void guardar(Connection conn, PagamentoCentral p) {
        String sql = "INSERT INTO PagamentoCentral (idOriginalLoja, idOriginalRemessa, idLoja, valor, dataPagamento, tipoPagamento) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE idOriginalRemessa = VALUES(idOriginalRemessa), valor = VALUES(valor), dataPagamento = VALUES(dataPagamento), tipoPagamento = VALUES(tipoPagamento)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getIdOriginalLoja());
            ps.setInt(2, p.getIdOriginalRemessa());
            ps.setString(3, p.getIdLoja());
            ps.setDouble(4, p.getValor());
            if (p.getDataPagamento() != null) ps.setDate(5, Date.valueOf(p.getDataPagamento()));
            else ps.setNull(5, Types.DATE);
            if (p.getTipoPagamento() != null) ps.setString(6, p.getTipoPagamento().name());
            else ps.setNull(6, Types.VARCHAR);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<PagamentoCentral> listarTodos() {
        List<PagamentoCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idOriginalLoja, idOriginalRemessa, idLoja, valor, dataPagamento, tipoPagamento FROM PagamentoCentral");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date dp = rs.getDate("dataPagamento");
                String tp = rs.getString("tipoPagamento");
                out.add(new PagamentoCentral(
                        rs.getInt("id"),
                        rs.getInt("idOriginalLoja"),
                        rs.getInt("idOriginalRemessa"),
                        rs.getString("idLoja"),
                        rs.getDouble("valor"),
                        dp != null ? dp.toLocalDate() : null,
                        tp != null ? TipoPagamento.valueOf(tp) : null
                ));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }
}
