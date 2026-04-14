package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;
import pt.trasmum.loja.dominio.fornecedores.Pagamento;
import pt.trasmum.loja.dominio.fornecedores.Pagamento.EstadoPagamento;
import pt.trasmum.loja.repositorio.interfaces.PagamentoRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoRepositorioImpl implements PagamentoRepositorio {

    private final Connection connection;

    public PagamentoRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(Pagamento pagamento) {
        String sql = "INSERT INTO Pagamento (idLoja, estadoSincronizacao, idFornecedor, idRemessa, idUtilizador, valor, dataHora, estadoPagamento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pagamento.getIdLoja());
            ps.setString(2, pagamento.getEstadoSincronizacao().name());
            ps.setInt(3, pagamento.getIdFornecedor());
            ps.setInt(4, pagamento.getIdRemessa());
            if (pagamento.getIdUtilizador() == 0) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, pagamento.getIdUtilizador());
            ps.setDouble(6, pagamento.getValor());
            if (pagamento.getDataHora() == null) ps.setNull(7, Types.TIMESTAMP);
            else ps.setTimestamp(7, Timestamp.valueOf(pagamento.getDataHora()));
            ps.setString(8, pagamento.getEstadoPagamento().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) pagamento.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar pagamento", e);
        }
    }

    @Override
    public List<Pagamento> buscarPendentes() {
        String revert = "UPDATE Pagamento SET estadoSincronizacao = 'PENDENTE' WHERE estadoSincronizacao = 'EM_TRANSITO'";
        try (PreparedStatement ps = connection.prepareStatement(revert)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reverter pagamentos em trânsito", e);
        }

        String sql = "SELECT * FROM Pagamento WHERE estadoSincronizacao = 'PENDENTE'";
        List<Pagamento> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pagamentos pendentes", e);
        }
        return lista;
    }

    @Override
    public void atualizar(Pagamento pagamento) {
        String sql = "UPDATE Pagamento SET estadoSincronizacao=?, idUtilizador=?, dataHora=?, estadoPagamento=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pagamento.getEstadoSincronizacao().name());
            if (pagamento.getIdUtilizador() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, pagamento.getIdUtilizador());
            if (pagamento.getDataHora() == null) ps.setNull(3, Types.TIMESTAMP);
            else ps.setTimestamp(3, Timestamp.valueOf(pagamento.getDataHora()));
            ps.setString(4, pagamento.getEstadoPagamento().name());
            ps.setInt(5, pagamento.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar pagamento", e);
        }
    }

    private Pagamento mapear(ResultSet rs) throws SQLException {
        Pagamento p = new Pagamento();
        p.setId(rs.getInt("id"));
        p.setIdLoja(rs.getString("idLoja"));
        p.setEstadoSincronizacao(EstadoSincronizacao.valueOf(rs.getString("estadoSincronizacao")));
        p.setIdFornecedor(rs.getInt("idFornecedor"));
        p.setIdRemessa(rs.getInt("idRemessa"));
        int idUtil = rs.getInt("idUtilizador");
        if (!rs.wasNull()) p.setIdUtilizador(idUtil);
        p.setValor(rs.getDouble("valor"));
        Timestamp ts = rs.getTimestamp("dataHora");
        if (ts != null) p.setDataHora(ts.toLocalDateTime());
        p.setEstadoPagamento(EstadoPagamento.valueOf(rs.getString("estadoPagamento")));
        return p;
    }
}
