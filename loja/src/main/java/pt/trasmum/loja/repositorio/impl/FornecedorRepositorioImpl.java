package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.fornecedores.Fornecedor;
import pt.trasmum.loja.repositorio.interfaces.FornecedorRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorRepositorioImpl implements FornecedorRepositorio {

    private final Connection connection;

    public FornecedorRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(Fornecedor fornecedor) {
        String sql = "INSERT INTO Fornecedor (nome, nif, morada, telefone, email, iban) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fornecedor.getNome());
            ps.setString(2, fornecedor.getNif());
            ps.setString(3, fornecedor.getMorada());
            ps.setString(4, fornecedor.getTelefone());
            ps.setString(5, fornecedor.getEmail());
            ps.setString(6, fornecedor.getIban());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) fornecedor.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar fornecedor", e);
        }
    }

    @Override
    public void atualizar(Fornecedor fornecedor) {
        String sql = "UPDATE Fornecedor SET nome=?, nif=?, morada=?, telefone=?, email=?, iban=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fornecedor.getNome());
            ps.setString(2, fornecedor.getNif());
            ps.setString(3, fornecedor.getMorada());
            ps.setString(4, fornecedor.getTelefone());
            ps.setString(5, fornecedor.getEmail());
            ps.setString(6, fornecedor.getIban());
            ps.setInt(7, fornecedor.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar fornecedor", e);
        }
    }

    @Override
    public Fornecedor buscarPorId(int id) {
        String sql = "SELECT * FROM Fornecedor WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar fornecedor por id", e);
        }
        return null;
    }

    @Override
    public List<Fornecedor> listarTodos() {
        String sql = "SELECT * FROM Fornecedor";
        List<Fornecedor> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar fornecedores", e);
        }
        return lista;
    }

    private Fornecedor mapear(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor();
        f.setId(rs.getInt("id"));
        f.setNome(rs.getString("nome"));
        f.setNif(rs.getString("nif"));
        f.setMorada(rs.getString("morada"));
        f.setTelefone(rs.getString("telefone"));
        f.setEmail(rs.getString("email"));
        f.setIban(rs.getString("iban"));
        return f;
    }
}
