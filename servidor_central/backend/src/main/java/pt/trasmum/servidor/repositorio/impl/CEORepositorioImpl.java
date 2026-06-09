package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.CEO;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.CEORepositorio;

import java.sql.*;

public class CEORepositorioImpl implements CEORepositorio {
    private final DatabaseConnection db;

    public CEORepositorioImpl(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public CEO buscarPorNomeUtilizador(String nomeUtilizador) {
        String sql = "SELECT id, nomeUtilizador, hashPalavraPasse, tentativasLogin, bloqueadoAte FROM CEO WHERE nomeUtilizador = ?";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nomeUtilizador);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Timestamp bloq = rs.getTimestamp("bloqueadoAte");
                return new CEO(
                        rs.getInt("id"),
                        rs.getString("nomeUtilizador"),
                        rs.getString("hashPalavraPasse"),
                        rs.getInt("tentativasLogin"),
                        bloq != null ? bloq.toLocalDateTime() : null
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar CEO: " + e.getMessage(), e);
        }
    }

    @Override
    public void atualizar(CEO ceo) {
        String sql = "UPDATE CEO SET hashPalavraPasse = ?, tentativasLogin = ?, bloqueadoAte = ? WHERE id = ?";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ceo.getHashPalavraPasse());
            ps.setInt(2, ceo.getTentativasLogin());
            if (ceo.getBloqueadoAte() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(ceo.getBloqueadoAte()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setInt(4, ceo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar CEO: " + e.getMessage(), e);
        }
    }

    @Override
    public void criarSeNaoExistir(CEO ceo) {
        String sql = "INSERT IGNORE INTO CEO (nomeUtilizador, hashPalavraPasse, tentativasLogin, bloqueadoAte) VALUES (?, ?, ?, ?)";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ceo.getNomeUtilizador());
            ps.setString(2, ceo.getHashPalavraPasse());
            ps.setInt(3, ceo.getTentativasLogin());
            if (ceo.getBloqueadoAte() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(ceo.getBloqueadoAte()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar CEO: " + e.getMessage(), e);
        }
    }

    @Override
    public void criar(CEO ceo) {
        String sql = "INSERT INTO CEO (nomeUtilizador, hashPalavraPasse, tentativasLogin, bloqueadoAte) VALUES (?, ?, 0, NULL)";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ceo.getNomeUtilizador());
            ps.setString(2, ceo.getHashPalavraPasse());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar CEO: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeContaDefinitiva() {
        String sql = "SELECT COUNT(*) FROM CEO WHERE nomeUtilizador != 'admin'";
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar conta definitiva: " + e.getMessage(), e);
        }
    }

    @Override
    public int contarTodos() {
        try (Connection c = db.obter(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM CEO");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar CEOs: " + e.getMessage(), e);
        }
    }

    @Override
    public void apagarPorNomeUtilizador(String nomeUtilizador) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement("DELETE FROM CEO WHERE nomeUtilizador = ?")) {
            ps.setString(1, nomeUtilizador);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao apagar CEO: " + e.getMessage(), e);
        }
    }
}
