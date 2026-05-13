package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.catalogo.Lote;
import pt.trasmum.loja.dominio.core.EstadoSincronizacao;
import pt.trasmum.loja.dominio.fornecedores.LinhaRemessa;
import pt.trasmum.loja.dominio.fornecedores.Remessa;
import pt.trasmum.loja.repositorio.interfaces.RemessaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RemessaRepositorioImpl implements RemessaRepositorio {

    private final Connection connection;

    public RemessaRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(Remessa remessa) {
        if (remessa.getLinhas() == null || remessa.getLinhas().isEmpty()) {
            throw new pt.trasmum.loja.dominio.exceptions.RemessaSemLinhasException("Uma remessa deve ter pelo menos uma linha");
        }

        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // 1. Persiste a Remessa
            String sqlRemessa = "INSERT INTO Remessa (idLoja, estadoSincronizacao, idFornecedor, idUtilizador, dataRecepcao, valorTotalGuia) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlRemessa, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, remessa.getIdLoja());
                ps.setString(2, remessa.getEstadoSincronizacao().name());
                ps.setInt(3, remessa.getIdFornecedor());
                ps.setInt(4, remessa.getIdUtilizador());
                ps.setDate(5, Date.valueOf(remessa.getDataRecepcao()));
                ps.setDouble(6, remessa.getValorTotalGuia());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) remessa.setId(keys.getInt(1));
                }
            }

            // 2. Para cada LinhaRemessa: cria Lote e atualiza idLoteGerado
            for (LinhaRemessa linha : remessa.getLinhas()) {
                linha.setIdRemessa(remessa.getId());

                // Cria o Lote usando o precoBase do Produto como precoVenda inicial
                Lote lote = new Lote(linha.getIdProduto(), linha.getQuantidade(), linha.getDataValidade(), 0.0);
                String sqlLote = "INSERT INTO Lote (idProduto, quantidade, dataValidade, precoVenda) " +
                                 "VALUES (?, ?, ?, (SELECT precoBase FROM Produto WHERE id = ?))";
                try (PreparedStatement ps = connection.prepareStatement(sqlLote, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, linha.getIdProduto());
                    ps.setInt(2, linha.getQuantidade());
                    if (linha.getDataValidade() != null) ps.setDate(3, Date.valueOf(linha.getDataValidade()));
                    else ps.setNull(3, Types.DATE);
                    ps.setInt(4, linha.getIdProduto());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            int idLote = keys.getInt(1);
                            lote.setId(idLote);
                            linha.setIdLoteGerado(idLote);
                        }
                    }
                }

                // Persiste a LinhaRemessa com o idLoteGerado
                String sqlLinha = "INSERT INTO LinhaRemessa (idRemessa, idProduto, idLoteGerado, quantidade, dataValidade) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sqlLinha, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, linha.getIdRemessa());
                    ps.setInt(2, linha.getIdProduto());
                    ps.setInt(3, linha.getIdLoteGerado());
                    ps.setInt(4, linha.getQuantidade());
                    if (linha.getDataValidade() != null) ps.setDate(5, Date.valueOf(linha.getDataValidade()));
                    else ps.setNull(5, Types.DATE);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) linha.setId(keys.getInt(1));
                    }
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { /* ignorar */ }
            throw new RuntimeException("Erro ao guardar remessa (transação revertida)", e);
        } finally {
            try { connection.setAutoCommit(autoCommit); } catch (SQLException e) { /* ignorar */ }
        }
    }

    @Override
    public void atualizarSincronizacao(int id, EstadoSincronizacao estado) {
        String sql = "UPDATE Remessa SET estadoSincronizacao = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar sincronização de remessa", e);
        }
    }

    @Override
    public void reverterEmTransito() {
        String sql = "UPDATE Remessa SET estadoSincronizacao = 'PENDENTE' WHERE estadoSincronizacao = 'EM_TRANSITO'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reverter remessas em trânsito", e);
        }
    }

    @Override
    public List<Remessa> buscarPendentes() {
        String sql = "SELECT * FROM Remessa WHERE estadoSincronizacao = 'PENDENTE'";
        List<Remessa> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearComLinhas(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar remessas pendentes", e);
        }
        return lista;
    }

    private Remessa mapearComLinhas(ResultSet rs) throws SQLException {
        Remessa r = new Remessa();
        r.setId(rs.getInt("id"));
        r.setIdLoja(rs.getString("idLoja"));
        r.setEstadoSincronizacao(EstadoSincronizacao.valueOf(rs.getString("estadoSincronizacao")));
        r.setIdFornecedor(rs.getInt("idFornecedor"));
        r.setIdUtilizador(rs.getInt("idUtilizador"));
        r.setDataRecepcao(rs.getDate("dataRecepcao").toLocalDate());
        r.setValorTotalGuia(rs.getDouble("valorTotalGuia"));
        r.setLinhas(buscarLinhas(r.getId()));
        return r;
    }

    private List<LinhaRemessa> buscarLinhas(int idRemessa) {
        String sql = "SELECT * FROM LinhaRemessa WHERE idRemessa = ?";
        List<LinhaRemessa> linhas = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idRemessa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinhaRemessa l = new LinhaRemessa();
                    l.setId(rs.getInt("id"));
                    l.setIdRemessa(rs.getInt("idRemessa"));
                    l.setIdProduto(rs.getInt("idProduto"));
                    l.setIdLoteGerado(rs.getInt("idLoteGerado"));
                    l.setQuantidade(rs.getInt("quantidade"));
                    Date dv = rs.getDate("dataValidade");
                    l.setDataValidade(dv != null ? dv.toLocalDate() : null);
                    linhas.add(l);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar linhas da remessa", e);
        }
        return linhas;
    }
}
