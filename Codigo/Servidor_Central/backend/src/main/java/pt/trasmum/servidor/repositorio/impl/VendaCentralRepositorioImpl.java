package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.LinhaVendaCentral;
import pt.trasmum.servidor.dominio.MetodoPagamento;
import pt.trasmum.servidor.dominio.VendaCentral;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.VendaCentralRepositorio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VendaCentralRepositorioImpl implements VendaCentralRepositorio {
    private final DatabaseConnection db;

    public VendaCentralRepositorioImpl(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public void guardar(VendaCentral venda) {
        Connection c = null;
        try {
            c = db.obter();
            c.setAutoCommit(false);
            guardar(c, venda);
            c.commit();
        } catch (SQLException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Erro ao guardar venda: " + e.getMessage(), e);
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); c.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public void guardar(Connection conn, VendaCentral venda) {
        String sqlVenda = "INSERT INTO VendaCentral (idOriginalLoja, idLoja, dataHora, totalFaturado, metodoPagamento) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE dataHora = VALUES(dataHora), totalFaturado = VALUES(totalFaturado), metodoPagamento = VALUES(metodoPagamento), id = LAST_INSERT_ID(id)";
        int idVenda;
        try (PreparedStatement ps = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, venda.getIdOriginalLoja());
            ps.setString(2, venda.getIdLoja());
            ps.setTimestamp(3, Timestamp.valueOf(venda.getDataHora()));
            ps.setDouble(4, venda.getTotalFaturado());
            ps.setString(5, venda.getMetodoPagamento().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                idVenda = keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar venda: " + e.getMessage(), e);
        }

        String sqlLinha = "INSERT INTO LinhaVendaCentral (idVendaCentral, idOriginalLoja, idLoja, nomeProduto, categoria, quantidade, precoUnitario, subtotal) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE idVendaCentral = VALUES(idVendaCentral), nomeProduto = VALUES(nomeProduto), categoria = VALUES(categoria), quantidade = VALUES(quantidade), precoUnitario = VALUES(precoUnitario), subtotal = VALUES(subtotal)";
        try (PreparedStatement ps = conn.prepareStatement(sqlLinha)) {
            for (LinhaVendaCentral l : venda.getLinhas()) {
                ps.setInt(1, idVenda);
                ps.setInt(2, l.getIdOriginalLoja());
                ps.setString(3, l.getIdLoja());
                ps.setString(4, l.getNomeProduto());
                ps.setString(5, l.getCategoria());
                ps.setInt(6, l.getQuantidade());
                ps.setDouble(7, l.getPrecoUnitario());
                ps.setDouble(8, l.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar linhas: " + e.getMessage(), e);
        }
    }

    private VendaCentral mapear(ResultSet rs) throws SQLException {
        return new VendaCentral(
                rs.getInt("id"),
                rs.getInt("idOriginalLoja"),
                rs.getString("idLoja"),
                rs.getTimestamp("dataHora").toLocalDateTime(),
                rs.getDouble("totalFaturado"),
                MetodoPagamento.valueOf(rs.getString("metodoPagamento")),
                new ArrayList<>()
        );
    }

    @Override
    public List<VendaCentral> buscarPorLoja(String idLoja) {
        List<VendaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idOriginalLoja, idLoja, dataHora, totalFaturado, metodoPagamento FROM VendaCentral WHERE idLoja = ?")) {
            ps.setString(1, idLoja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public List<VendaCentral> buscarPorPeriodo(LocalDate inicio, LocalDate fim) {
        List<VendaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idOriginalLoja, idLoja, dataHora, totalFaturado, metodoPagamento FROM VendaCentral WHERE DATE(dataHora) BETWEEN ? AND ?")) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public List<VendaCentral> buscarPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim) {
        List<VendaCentral> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, idOriginalLoja, idLoja, dataHora, totalFaturado, metodoPagamento FROM VendaCentral WHERE idLoja = ? AND DATE(dataHora) BETWEEN ? AND ?")) {
            ps.setString(1, idLoja);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public double totalVendasPorPeriodo(LocalDate inicio, LocalDate fim) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(totalFaturado), 0) FROM VendaCentral WHERE DATE(dataHora) BETWEEN ? AND ?")) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public double totalVendasPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(totalFaturado), 0) FROM VendaCentral WHERE idLoja = ? AND DATE(dataHora) BETWEEN ? AND ?")) {
            ps.setString(1, idLoja);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int totalTransacoesPorPeriodo(LocalDate inicio, LocalDate fim) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM VendaCentral WHERE DATE(dataHora) BETWEEN ? AND ?")) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int totalTransacoesPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM VendaCentral WHERE idLoja = ? AND DATE(dataHora) BETWEEN ? AND ?")) {
            ps.setString(1, idLoja);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Map<String, Double> vendasPorCategoria(LocalDate inicio, LocalDate fim) {
        Map<String, Double> out = new LinkedHashMap<>();
        String sql = "SELECT lv.categoria, COALESCE(SUM(lv.subtotal), 0) AS total " +
                "FROM LinhaVendaCentral lv JOIN VendaCentral v ON lv.idVendaCentral = v.id " +
                "WHERE DATE(v.dataHora) BETWEEN ? AND ? " +
                "GROUP BY lv.categoria ORDER BY total DESC";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.put(rs.getString("categoria"), rs.getDouble("total"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public Map<String, Double> vendasPorCategoriaPorLoja(String idLoja, LocalDate inicio, LocalDate fim) {
        Map<String, Double> out = new LinkedHashMap<>();
        String sql = "SELECT lv.categoria, COALESCE(SUM(lv.subtotal), 0) AS total " +
                "FROM LinhaVendaCentral lv JOIN VendaCentral v ON lv.idVendaCentral = v.id " +
                "WHERE v.idLoja = ? AND DATE(v.dataHora) BETWEEN ? AND ? " +
                "GROUP BY lv.categoria ORDER BY total DESC";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, idLoja);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.put(rs.getString("categoria"), rs.getDouble("total"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public Map<String, Double> vendasMensaisPorLoja(String idLoja, int ano) {
        Map<String, Double> out = new LinkedHashMap<>();
        String[] meses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        for (String m : meses) out.put(m, 0.0);
        String sql = "SELECT MONTH(dataHora) AS mes, COALESCE(SUM(totalFaturado), 0) AS total " +
                "FROM VendaCentral WHERE idLoja = ? AND YEAR(dataHora) = ? GROUP BY MONTH(dataHora)";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, idLoja);
            ps.setInt(2, ano);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    if (mes >= 1 && mes <= 12) out.put(meses[mes - 1], rs.getDouble("total"));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public Map<String, Double> totaisPorMetodoPagamento(LocalDate inicio, LocalDate fim) {
        Map<String, Double> out = new LinkedHashMap<>();
        out.put("NUMERARIO", 0.0);
        out.put("MULTIBANCO", 0.0);
        String sql = "SELECT metodoPagamento, COALESCE(SUM(totalFaturado), 0) AS total FROM VendaCentral " +
                "WHERE DATE(dataHora) BETWEEN ? AND ? GROUP BY metodoPagamento";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fim));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.put(rs.getString("metodoPagamento"), rs.getDouble("total"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    @Override
    public List<Object[]> relatorioAgregado(String idLoja, LocalDate inicio, LocalDate fim, String categoria) {
        List<Object[]> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT DATE(v.dataHora) AS dia, v.idLoja, l.nomeLoja, lv.categoria, COALESCE(SUM(lv.subtotal), 0) AS total " +
                        "FROM LinhaVendaCentral lv " +
                        "JOIN VendaCentral v ON lv.idVendaCentral = v.id " +
                        "JOIN Loja l ON v.idLoja = l.idLoja WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (idLoja != null && !idLoja.isBlank()) { sql.append("AND v.idLoja = ? "); params.add(idLoja); }
        if (inicio != null) { sql.append("AND DATE(v.dataHora) >= ? "); params.add(Date.valueOf(inicio)); }
        if (fim != null) { sql.append("AND DATE(v.dataHora) <= ? "); params.add(Date.valueOf(fim)); }
        if (categoria != null && !categoria.isBlank()) { sql.append("AND lv.categoria = ? "); params.add(categoria); }
        sql.append("GROUP BY dia, v.idLoja, l.nomeLoja, lv.categoria ORDER BY dia DESC, v.idLoja, lv.categoria");

        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Object[]{
                            rs.getDate("dia").toLocalDate(),
                            rs.getString("idLoja"),
                            rs.getString("nomeLoja"),
                            rs.getString("categoria"),
                            rs.getDouble("total")
                    });
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }
}
