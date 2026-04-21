package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;
import pt.trasmum.loja.dominio.vendas.Devolucao;
import pt.trasmum.loja.repositorio.interfaces.DevolucaoRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DevolucaoRepositorioImpl implements DevolucaoRepositorio {

    private final Connection connection;

    public DevolucaoRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(Devolucao devolucao) {
        String sql = "INSERT INTO Devolucao (idLoja, estadoSincronizacao, idFatura, idUtilizador, idLote, dataHora, quantidade, dataValidadeEmbalagem, valorRestituido) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, devolucao.getIdLoja());
            ps.setString(2, devolucao.getEstadoSincronizacao().name());
            ps.setInt(3, devolucao.getIdFatura());
            ps.setInt(4, devolucao.getIdUtilizador());
            ps.setInt(5, devolucao.getIdLote());
            ps.setTimestamp(6, Timestamp.valueOf(devolucao.getDataHora()));
            ps.setInt(7, devolucao.getQuantidade());
            ps.setDate(8, Date.valueOf(devolucao.getDataValidadeEmbalagem()));
            ps.setDouble(9, devolucao.getValorRestituido());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) devolucao.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar devolução", e);
        }
    }

    @Override
    public void atualizarSincronizacao(int id, EstadoSincronizacao estado) {
        String sql = "UPDATE Devolucao SET estadoSincronizacao = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar sincronização de devolução", e);
        }
    }

    @Override
    public List<Devolucao> buscarPorFatura(String numeroFatura) {
        String sql = "SELECT d.* FROM Devolucao d INNER JOIN Fatura f ON f.id = d.idFatura WHERE f.numeroFatura = ?";
        List<Devolucao> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, numeroFatura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar devoluções por fatura", e);
        }
        return lista;
    }

    @Override
    public void reverterEmTransito() {
        String sql = "UPDATE Devolucao SET estadoSincronizacao = 'PENDENTE' WHERE estadoSincronizacao = 'EM_TRANSITO'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reverter devoluções em trânsito", e);
        }
    }

    @Override
    public List<Devolucao> buscarPendentes() {
        String sql = "SELECT * FROM Devolucao WHERE estadoSincronizacao = 'PENDENTE'";
        List<Devolucao> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar devoluções pendentes", e);
        }
        return lista;
    }

    private Devolucao mapear(ResultSet rs) throws SQLException {
        Devolucao d = new Devolucao();
        d.setId(rs.getInt("id"));
        d.setIdLoja(rs.getString("idLoja"));
        d.setEstadoSincronizacao(EstadoSincronizacao.valueOf(rs.getString("estadoSincronizacao")));
        d.setIdFatura(rs.getInt("idFatura"));
        d.setIdUtilizador(rs.getInt("idUtilizador"));
        d.setIdLote(rs.getInt("idLote"));
        d.setDataHora(rs.getTimestamp("dataHora").toLocalDateTime());
        d.setQuantidade(rs.getInt("quantidade"));
        d.setDataValidadeEmbalagem(rs.getDate("dataValidadeEmbalagem").toLocalDate());
        d.setValorRestituido(rs.getDouble("valorRestituido"));
        return d;
    }
}
