package pt.trasmum.loja.repositorio.impl;

import org.mindrot.jbcrypt.BCrypt;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.repositorio.interfaces.UtilizadorRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilizadorRepositorioImpl implements UtilizadorRepositorio {

    private final Connection connection;

    public UtilizadorRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Utilizador buscarPorCredenciais(String nomeUtilizador, String palavraPasse) {
        String sql = "SELECT * FROM Utilizador WHERE nomeUtilizador = ? AND ativo = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomeUtilizador);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashArmazenado = rs.getString("hashPalavraPasse");
                    if (BCrypt.checkpw(palavraPasse, hashArmazenado)) {
                        return mapearUtilizador(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar utilizador por credenciais", e);
        }
        return null;
    }

    @Override
    public Utilizador buscarPorId(int id) {
        String sql = "SELECT * FROM Utilizador WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearUtilizador(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar utilizador por id", e);
        }
        return null;
    }

    @Override
    public void guardar(Utilizador utilizador) {
        String sql = "INSERT INTO Utilizador (nomeUtilizador, hashPalavraPasse, perfil, ativo, emSessao) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, utilizador.getNomeUtilizador());
            ps.setString(2, utilizador.getHashPalavraPasse());
            ps.setString(3, utilizador.getPerfil().name());
            ps.setBoolean(4, utilizador.isAtivo());
            ps.setBoolean(5, utilizador.isEmSessao());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) utilizador.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar utilizador", e);
        }
    }

    @Override
    public void atualizar(Utilizador utilizador) {
        String sql = "UPDATE Utilizador SET nomeUtilizador=?, hashPalavraPasse=?, perfil=?, ativo=?, emSessao=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, utilizador.getNomeUtilizador());
            ps.setString(2, utilizador.getHashPalavraPasse());
            ps.setString(3, utilizador.getPerfil().name());
            ps.setBoolean(4, utilizador.isAtivo());
            ps.setBoolean(5, utilizador.isEmSessao());
            ps.setInt(6, utilizador.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar utilizador", e);
        }
    }

    @Override
    public void desativar(int id) {
        String sql = "UPDATE Utilizador SET ativo = 0 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar utilizador", e);
        }
    }

    @Override
    public boolean verificarSessaoAtiva(int id) {
        String sql = "SELECT emSessao FROM Utilizador WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("emSessao");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar sessão ativa", e);
        }
        return false;
    }

    @Override
    public void limparSessoesAtivas() {
        String sql = "UPDATE Utilizador SET emSessao = 0 WHERE emSessao = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao limpar sessões ativas", e);
        }
    }

    @Override
    public List<Utilizador> listarAtivos() {
        String sql = "SELECT * FROM Utilizador WHERE ativo = 1";
        List<Utilizador> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearUtilizador(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar utilizadores ativos", e);
        }
        return lista;
    }

    private Utilizador mapearUtilizador(ResultSet rs) throws SQLException {
        Utilizador u = new Utilizador();
        u.setId(rs.getInt("id"));
        u.setNomeUtilizador(rs.getString("nomeUtilizador"));
        u.setHashPalavraPasse(rs.getString("hashPalavraPasse"));
        u.setPerfil(PerfilUtilizador.valueOf(rs.getString("perfil")));
        u.setAtivo(rs.getBoolean("ativo"));
        u.setEmSessao(rs.getBoolean("emSessao"));
        return u;
    }
}
