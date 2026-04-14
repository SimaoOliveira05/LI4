package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;
import pt.trasmum.loja.dominio.vendas.*;
import pt.trasmum.loja.dominio.vendas.Venda.MetodoPagamento;
import pt.trasmum.loja.dominio.vendas.Venda.EstadoVenda;
import pt.trasmum.loja.repositorio.interfaces.VendaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendaRepositorioImpl implements VendaRepositorio {

    private final Connection connection;

    public VendaRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(Venda venda) {
        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // 1. Persiste a Venda
            String sqlVenda = "INSERT INTO Venda (idLoja, estadoSincronizacao, idUtilizador, dataHora, metodoPagamento, totalFaturado, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, venda.getIdLoja());
                ps.setString(2, venda.getEstadoSincronizacao().name());
                ps.setInt(3, venda.getIdUtilizador());
                ps.setTimestamp(4, Timestamp.valueOf(venda.getDataHora()));
                ps.setString(5, venda.getMetodoPagamento().name());
                ps.setDouble(6, venda.getTotalFaturado());
                ps.setString(7, venda.getEstado().name());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) venda.setId(keys.getInt(1));
                }
            }

            // 2. Persiste cada LinhaVenda
            String sqlLinha = "INSERT INTO LinhaVenda (idVenda, idLote, quantidade, precoUnitario) VALUES (?, ?, ?, ?)";
            for (LinhaVenda linha : venda.getLinhas()) {
                linha.setIdVenda(venda.getId());
                try (PreparedStatement ps = connection.prepareStatement(sqlLinha, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, linha.getIdVenda());
                    ps.setInt(2, linha.getIdLote());
                    ps.setInt(3, linha.getQuantidade());
                    ps.setDouble(4, linha.getPrecoUnitario());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) linha.setId(keys.getInt(1));
                    }
                }
            }

            // 3. Persiste a Fatura (se existir)
            if (venda.getFatura() != null) {
                Fatura fatura = venda.getFatura();
                fatura.setIdVenda(venda.getId());
                String sqlFatura = "INSERT INTO Fatura (idVenda, numeroFatura, dataEmissao, nifCliente) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sqlFatura, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, fatura.getIdVenda());
                    ps.setString(2, fatura.getNumeroFatura());
                    ps.setTimestamp(3, Timestamp.valueOf(fatura.getDataEmissao()));
                    if (fatura.getNifCliente() == null) ps.setNull(4, Types.VARCHAR);
                    else ps.setString(4, fatura.getNifCliente());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) fatura.setId(keys.getInt(1));
                    }
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignorar */ }
            throw new RuntimeException("Erro ao guardar venda (transação revertida)", e);
        } finally {
            try { connection.setAutoCommit(autoCommit); } catch (SQLException e) { /* ignorar */ }
        }
    }

    @Override
    public Venda buscarPorId(int id) {
        String sql = "SELECT * FROM Venda WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearComDetalhes(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar venda por id", e);
        }
        return null;
    }

    @Override
    public Venda buscarPorNumeroFatura(String numeroFatura) {
        String sql = "SELECT v.* FROM Venda v INNER JOIN Fatura f ON f.idVenda = v.id WHERE f.numeroFatura = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, numeroFatura);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearComDetalhes(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar venda por número de fatura", e);
        }
        return null;
    }

    @Override
    public List<Venda> buscarPendentes() {
        String revert = "UPDATE Venda SET estadoSincronizacao = 'PENDENTE' WHERE estadoSincronizacao = 'EM_TRANSITO'";
        try (PreparedStatement ps = connection.prepareStatement(revert)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reverter vendas em trânsito", e);
        }

        String sql = "SELECT * FROM Venda WHERE estadoSincronizacao = 'PENDENTE'";
        List<Venda> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearComDetalhes(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar vendas pendentes", e);
        }
        return lista;
    }

    @Override
    public void atualizarSincronizacao(int id, EstadoSincronizacao estado) {
        String sql = "UPDATE Venda SET estadoSincronizacao = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar sincronização de venda", e);
        }
    }

    @Override
    public void marcarConfirmadas(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        StringBuilder sb = new StringBuilder("UPDATE Venda SET estadoSincronizacao = 'CONFIRMADO' WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(i > 0 ? ",?" : "?");
        }
        sb.append(")");
        try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao marcar vendas como confirmadas", e);
        }
    }

    private Venda mapearComDetalhes(ResultSet rs) throws SQLException {
        Venda v = new Venda();
        v.setId(rs.getInt("id"));
        v.setIdLoja(rs.getString("idLoja"));
        v.setEstadoSincronizacao(EstadoSincronizacao.valueOf(rs.getString("estadoSincronizacao")));
        v.setIdUtilizador(rs.getInt("idUtilizador"));
        v.setDataHora(rs.getTimestamp("dataHora").toLocalDateTime());
        v.setMetodoPagamento(MetodoPagamento.valueOf(rs.getString("metodoPagamento")));
        v.setTotalFaturado(rs.getDouble("totalFaturado"));
        v.setEstado(EstadoVenda.valueOf(rs.getString("estado")));
        v.setLinhas(buscarLinhas(v.getId()));
        v.setFatura(buscarFatura(v.getId()));
        return v;
    }

    private List<LinhaVenda> buscarLinhas(int idVenda) {
        String sql = "SELECT * FROM LinhaVenda WHERE idVenda = ?";
        List<LinhaVenda> linhas = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVenda);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinhaVenda l = new LinhaVenda();
                    l.setId(rs.getInt("id"));
                    l.setIdVenda(rs.getInt("idVenda"));
                    l.setIdLote(rs.getInt("idLote"));
                    l.setQuantidade(rs.getInt("quantidade"));
                    l.setPrecoUnitario(rs.getDouble("precoUnitario"));
                    linhas.add(l);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar linhas da venda", e);
        }
        return linhas;
    }

    private Fatura buscarFatura(int idVenda) {
        String sql = "SELECT * FROM Fatura WHERE idVenda = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVenda);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Fatura f = new Fatura();
                    f.setId(rs.getInt("id"));
                    f.setIdVenda(rs.getInt("idVenda"));
                    f.setNumeroFatura(rs.getString("numeroFatura"));
                    f.setDataEmissao(rs.getTimestamp("dataEmissao").toLocalDateTime());
                    f.setNifCliente(rs.getString("nifCliente"));
                    return f;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar fatura", e);
        }
        return null;
    }
}
