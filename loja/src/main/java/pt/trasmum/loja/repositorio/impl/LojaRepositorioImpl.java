package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.core.Loja;
import pt.trasmum.loja.repositorio.interfaces.LojaRepositorio;

import java.sql.*;

public class LojaRepositorioImpl implements LojaRepositorio {

    private final Connection connection;

    public LojaRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Loja obter(String idLoja) {
        String sql = "SELECT * FROM ConfiguracaoLoja WHERE idLoja = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, idLoja);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter configuração da loja", e);
        }
        return null;
    }

    @Override
    public void guardar(Loja loja) {
        String sql = "INSERT INTO ConfiguracaoLoja (idLoja, nome, morada, localidade, nif, email, limiteMaximoCaixa, diasAlertaValidade) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, loja.getId());
            ps.setString(2, loja.getNome());
            ps.setString(3, loja.getMorada());
            ps.setString(4, loja.getLocalidade());
            ps.setString(5, loja.getNif());
            ps.setString(6, loja.getEmail());
            ps.setDouble(7, loja.getLimiteMaximoCaixa());
            ps.setInt(8, loja.getDiasAlertaValidade());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar configuração da loja", e);
        }
    }

    @Override
    public void atualizar(Loja loja) {
        String sql = "UPDATE ConfiguracaoLoja SET nome=?, morada=?, localidade=?, nif=?, email=?, limiteMaximoCaixa=?, diasAlertaValidade=? WHERE idLoja=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, loja.getNome());
            ps.setString(2, loja.getMorada());
            ps.setString(3, loja.getLocalidade());
            ps.setString(4, loja.getNif());
            ps.setString(5, loja.getEmail());
            ps.setDouble(6, loja.getLimiteMaximoCaixa());
            ps.setInt(7, loja.getDiasAlertaValidade());
            ps.setString(8, loja.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar configuração da loja", e);
        }
    }

    private Loja mapear(ResultSet rs) throws SQLException {
        return new Loja(
                rs.getString("idLoja"),
                rs.getString("nome"),
                rs.getString("morada"),
                rs.getString("localidade"),
                rs.getString("nif"),
                rs.getString("email"),
                rs.getDouble("limiteMaximoCaixa"),
                rs.getInt("diasAlertaValidade")
        );
    }
}
