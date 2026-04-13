package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;
import pt.trasmum.loja.dominio.core.LogAuditoria;
import pt.trasmum.loja.dominio.core.TipoAcao;
import pt.trasmum.loja.repositorio.interfaces.LogAuditoriaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogAuditoriaRepositorioImpl implements LogAuditoriaRepositorio {

    private final Connection connection;

    public LogAuditoriaRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(LogAuditoria log) {
        String sql = "INSERT INTO LogAuditoria (idLoja, estadoSincronizacao, acao, dataHora, idUtilizador, entidade, idEntidade) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, log.getIdLoja());
            ps.setString(2, log.getEstadoSincronizacao().name());
            ps.setString(3, log.getAcao().name());
            ps.setTimestamp(4, Timestamp.valueOf(log.getDataHora()));
            ps.setInt(5, log.getIdUtilizador());
            ps.setString(6, log.getEntidade());
            ps.setInt(7, log.getIdEntidade());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) log.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar log de auditoria", e);
        }
    }

    @Override
    public void atualizar(LogAuditoria log) {
        String sql = "UPDATE LogAuditoria SET estadoSincronizacao = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, log.getEstadoSincronizacao().name());
            ps.setInt(2, log.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar log de auditoria", e);
        }
    }

    @Override
    public List<LogAuditoria> buscarPendentes() {
        // Reverte EM_TRANSITO para PENDENTE (recuperação após falha)
        String revert = "UPDATE LogAuditoria SET estadoSincronizacao = 'PENDENTE' WHERE estadoSincronizacao = 'EM_TRANSITO'";
        try (PreparedStatement ps = connection.prepareStatement(revert)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reverter logs em trânsito", e);
        }

        String sql = "SELECT * FROM LogAuditoria WHERE estadoSincronizacao = 'PENDENTE'";
        List<LogAuditoria> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar logs pendentes", e);
        }
        return lista;
    }

    private LogAuditoria mapear(ResultSet rs) throws SQLException {
        LogAuditoria log = new LogAuditoria();
        log.setId(rs.getInt("id"));
        log.setIdLoja(rs.getString("idLoja"));
        log.setEstadoSincronizacao(EstadoSincronizacao.valueOf(rs.getString("estadoSincronizacao")));
        log.setAcao(TipoAcao.valueOf(rs.getString("acao")));
        log.setDataHora(rs.getTimestamp("dataHora").toLocalDateTime());
        log.setIdUtilizador(rs.getInt("idUtilizador"));
        log.setEntidade(rs.getString("entidade"));
        log.setIdEntidade(rs.getInt("idEntidade"));
        return log;
    }
}
