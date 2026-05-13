package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;
import pt.trasmum.loja.dominio.tesouraria.FechoDia;
import pt.trasmum.loja.repositorio.interfaces.FechoDiaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FechoDiaRepositorioImpl implements FechoDiaRepositorio {

    private final Connection connection;

    public FechoDiaRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(FechoDia fecho) {
        String sql = "INSERT INTO FechoDia (idLoja, estadoSincronizacao, idUtilizador, dataFecho) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fecho.getIdLoja());
            ps.setString(2, fecho.getEstadoSincronizacao().name());
            ps.setInt(3, fecho.getIdUtilizador());
            ps.setDate(4, Date.valueOf(fecho.getDataFecho()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) fecho.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar fecho de dia", e);
        }
    }

    @Override
    public void atualizar(FechoDia fecho) {
        String sql = "UPDATE FechoDia SET estadoSincronizacao = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fecho.getEstadoSincronizacao().name());
            ps.setInt(2, fecho.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar fecho de dia", e);
        }
    }

    @Override
    public FechoDia buscarUltimoConfirmado() {
        String sql = "SELECT * FROM FechoDia WHERE estadoSincronizacao = 'CONFIRMADO' ORDER BY dataFecho DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar último fecho confirmado", e);
        }
        return null;
    }

    @Override
    public void reverterEmTransito() {
        String sql = "UPDATE FechoDia SET estadoSincronizacao = 'PENDENTE' WHERE estadoSincronizacao = 'EM_TRANSITO'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reverter fechos em trânsito", e);
        }
    }

    @Override
    public List<FechoDia> buscarPendentes() {
        String sql = "SELECT * FROM FechoDia WHERE estadoSincronizacao = 'PENDENTE'";
        List<FechoDia> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar fechos pendentes", e);
        }
        return lista;
    }

    private FechoDia mapear(ResultSet rs) throws SQLException {
        FechoDia f = new FechoDia();
        f.setId(rs.getInt("id"));
        f.setIdLoja(rs.getString("idLoja"));
        f.setEstadoSincronizacao(EstadoSincronizacao.valueOf(rs.getString("estadoSincronizacao")));
        f.setIdUtilizador(rs.getInt("idUtilizador"));
        f.setDataFecho(rs.getDate("dataFecho").toLocalDate());
        return f;
    }
}
