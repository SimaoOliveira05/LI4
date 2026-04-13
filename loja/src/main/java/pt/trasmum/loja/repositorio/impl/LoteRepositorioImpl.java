package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.catalogo.Desconto;
import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.repositorio.interfaces.LoteRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoteRepositorioImpl implements LoteRepositorio {

    private final Connection connection;

    public LoteRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(Lote lote) {
        String sql = "INSERT INTO Lote (idProduto, quantidade, dataValidade, precoVenda) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lote.getIdProduto());
            ps.setInt(2, lote.getQuantidade());
            ps.setDate(3, Date.valueOf(lote.getDataValidade()));
            ps.setDouble(4, lote.getPrecoVenda());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) lote.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar lote", e);
        }
    }

    @Override
    public void atualizar(Lote lote) {
        String sql = "UPDATE Lote SET idProduto=?, quantidade=?, dataValidade=?, precoVenda=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, lote.getIdProduto());
            ps.setInt(2, lote.getQuantidade());
            ps.setDate(3, Date.valueOf(lote.getDataValidade()));
            ps.setDouble(4, lote.getPrecoVenda());
            ps.setInt(5, lote.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar lote", e);
        }
    }

    @Override
    public List<Lote> buscarLotesFEFO(int idProduto) {
        String sql = "SELECT l.*, d.id as dId, d.percentagem, d.dataAplicacao, d.idUtilizadorAplicou " +
                     "FROM Lote l LEFT JOIN Desconto d ON d.idLote = l.id " +
                     "WHERE l.idProduto = ? AND l.quantidade > 0 ORDER BY l.dataValidade ASC";
        List<Lote> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idProduto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearComDesconto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar lotes FEFO", e);
        }
        return lista;
    }

    @Override
    public List<Lote> buscarLotesComDesconto() {
        String sql = "SELECT l.*, d.id as dId, d.percentagem, d.dataAplicacao, d.idUtilizadorAplicou " +
                     "FROM Lote l INNER JOIN Desconto d ON d.idLote = l.id WHERE l.quantidade > 0";
        List<Lote> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearComDesconto(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar lotes com desconto", e);
        }
        return lista;
    }

    @Override
    public List<Lote> buscarAbaixoDaValidade(int diasLimite) {
        String sql = "SELECT l.*, d.id as dId, d.percentagem, d.dataAplicacao, d.idUtilizadorAplicou " +
                     "FROM Lote l LEFT JOIN Desconto d ON d.idLote = l.id " +
                     "WHERE l.quantidade > 0 AND l.dataValidade <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "ORDER BY l.dataValidade ASC";
        List<Lote> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, diasLimite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearComDesconto(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar lotes abaixo da validade", e);
        }
        return lista;
    }

    @Override
    public Map<Integer, Integer> stockPorProduto() {
        String sql = "SELECT idProduto, COALESCE(SUM(quantidade), 0) AS total FROM Lote GROUP BY idProduto";
        Map<Integer, Integer> mapa = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) mapa.put(rs.getInt("idProduto"), rs.getInt("total"));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular stock por produto", e);
        }
        return mapa;
    }

    @Override
    public void guardarDesconto(Desconto desconto, int idLote) {
        String sql = "INSERT INTO Desconto (idLote, percentagem, dataAplicacao, idUtilizadorAplicou) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idLote);
            ps.setDouble(2, desconto.getPercentagem());
            ps.setDate(3, Date.valueOf(desconto.getDataAplicacao()));
            ps.setInt(4, desconto.getIdUtilizadorAplicou());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) desconto.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar desconto", e);
        }
    }

    private Lote mapearComDesconto(ResultSet rs) throws SQLException {
        Lote lote = new Lote();
        lote.setId(rs.getInt("id"));
        lote.setIdProduto(rs.getInt("idProduto"));
        lote.setQuantidade(rs.getInt("quantidade"));
        lote.setDataValidade(rs.getDate("dataValidade").toLocalDate());
        lote.setPrecoVenda(rs.getDouble("precoVenda"));

        int dId = rs.getInt("dId");
        if (!rs.wasNull()) {
            Desconto d = new Desconto();
            d.setId(dId);
            d.setIdLote(lote.getId());
            d.setPercentagem(rs.getDouble("percentagem"));
            Date da = rs.getDate("dataAplicacao");
            if (da != null) d.setDataAplicacao(da.toLocalDate());
            d.setIdUtilizadorAplicou(rs.getInt("idUtilizadorAplicou"));
            lote.aplicarDesconto(d);
        }
        return lote;
    }
}
